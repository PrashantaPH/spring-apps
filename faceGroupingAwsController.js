
// src/controllers/faceGroupingAwsController.js
AWS_ACCESS_KEY_ID=AKIAZI2LFOSDSHKCS2UM
AWS_SECRET_ACCESS_KEY=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
AWS_REGION=ap-south-1
REKOG_COLLECTION_PREFIX=eventsnap
FACE_MATCH_THRESHOLD=95
REKOG_QUALITY_FILTER=HIGH
REKOG_MAX_RESULTS=20

import {
  RekognitionClient,
  CreateCollectionCommand,
  DescribeCollectionCommand,
  IndexFacesCommand,
  SearchFacesCommand,
  DetectFacesCommand,
} from "@aws-sdk/client-rekognition";

import Document from "../models/Document.js";

const rekog = new RekognitionClient({
  region: process.env.AWS_REGION || "ap-south-1",
});

function collectionIdForEvent(eventId) {
  const prefix = process.env.REKOG_COLLECTION_PREFIX || "eventsnap";
  return `${prefix}-${eventId}`;
}

async function ensureCollection(collectionId) {
  try {
    await rekog.send(
      new DescribeCollectionCommand({ CollectionId: collectionId })
    );
    return;
  } catch {
    // collection does not exist → create
  }

  await rekog.send(
    new CreateCollectionCommand({ CollectionId: collectionId })
  );
}

/**
 * GET /events/:eventId/faces-aws
 */
const faceGroupingByEventAws = async (req, res) => {
  const { eventId } = req.params;

  const FACE_MATCH_THRESHOLD = Number(
    process.env.FACE_MATCH_THRESHOLD || 95
  );
  const QUALITY_FILTER = process.env.REKOG_QUALITY_FILTER || "HIGH";
  const MAX_RESULTS_PER_SEARCH = Number(
    process.env.REKOG_MAX_RESULTS || 20
  );

  if (!eventId) {
    return res
      .status(400)
      .json({ success: false, message: "Event ID is required" });
  }

  try {
    const docs = await Document.find({ eventId });
    if (!docs.length) {
      return res.status(404).json({
        success: false,
        message: "No documents found for this event",
      });
    }

    const collectionId = collectionIdForEvent(eventId);
    await ensureCollection(collectionId);

    const imageDocs = docs.filter(
      (d) => d.fileType === "image" && Buffer.isBuffer(d.fileData)
    );

    const faceIdToDocInfo = new Map();

    // 1. Detect faces first (skip images with no faces)
    for (const doc of imageDocs) {
      const bytes = doc.fileData;

      let hasFace = false;
      try {
        const detect = await rekog.send(
          new DetectFacesCommand({
            Image: { Bytes: bytes },
            Attributes: ["DEFAULT"],
          })
        );

        hasFace = (detect.FaceDetails?.length || 0) > 0;
      } catch (e) {
        console.warn("DetectFaces failed:", e?.name || e);
        continue;
      }

      if (!hasFace) continue;

      // 2. Index faces
      try {
        const idx = await rekog.send(
          new IndexFacesCommand({
            CollectionId: collectionId,
            Image: { Bytes: bytes },
            ExternalImageId: doc.imageId,
            QualityFilter: QUALITY_FILTER,
            DetectionAttributes: ["DEFAULT"],
          })
        );

        if (idx.FaceRecords) {
          idx.FaceRecords.forEach((record) => {
            const fid = record?.Face?.FaceId;
            if (fid)
              faceIdToDocInfo.set(fid, {
                imageId: doc.imageId,
                fileName: doc.fileName,
              });
          });
        }
      } catch (e) {
        console.warn("IndexFaces failed:", e?.name || e);
      }
    }

    const allFaceIds = Array.from(faceIdToDocInfo.keys());
    if (!allFaceIds.length) {
      return res.json({ success: true, eventId, faces: [] });
    }

    // 3. Cluster faces using SearchFaces
    const visited = new Set();
    const clusters = [];

    for (const seed of allFaceIds) {
      if (visited.has(seed)) continue;

      const queue = [seed];
      const cluster = new Set([seed]);
      visited.add(seed);

      while (queue.length) {
        const current = queue.shift();

        try {
          const resp = await rekog.send(
            new SearchFacesCommand({
              CollectionId: collectionId,
              FaceId: current,
              FaceMatchThreshold: FACE_MATCH_THRESHOLD,
              MaxFaces: MAX_RESULTS_PER_SEARCH,
            })
          );

          const neighbors = (resp.FaceMatches || [])
            .map((m) => m?.Face?.FaceId)
            .filter(Boolean);

          neighbors.forEach((n) => {
            if (!visited.has(n)) {
              visited.add(n);
              cluster.add(n);
              queue.push(n);
            }
          });
        } catch (e) {
          console.warn("SearchFaces failed:", e?.name || e);
        }
      }

      clusters.push([...cluster]);
    }

    // 4. Build response including base64 fileData inside images[]
    const faces = clusters.map((faceIds, i) => {
      const images = [];

      for (const fid of faceIds) {
        const info = faceIdToDocInfo.get(fid);
        if (info) {
          const matchingDoc = docs.find(
            (d) => d.imageId === info.imageId
          );

          images.push({
            imageId: info.imageId,
            fileName: info.fileName,
            fileData: matchingDoc?.fileData
              ? matchingDoc.fileData.toString("base64")
              : null,
          });
        }
      }

      const avatar =
        images.length > 0 ? images[0].fileData : null;

      return {
        groupId: `g${i + 1}`,
        faceIds,
        avatar,
        images,
      };
    });

    return res.json({
      success: true,
      eventId,
      threshold: FACE_MATCH_THRESHOLD,
      qualityFilter: QUALITY_FILTER,
      faces,
    });
  } catch (error) {
    console.error(error);
    return res
      .status(500)
      .json({ success: false, message: "Server error" });
  }
};

export default faceGroupingByEventAws;
