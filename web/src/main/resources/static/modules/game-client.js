import * as protobuf from "./protobuf.js";

function getProtobuf() {
  return window.protobuf ? window.protobuf : protobuf;
}

export async function testLoad() {
  const protobuf = getProtobuf();
  const result = await protobuf.load("../dotsboxes_service.proto");
  return result;
}