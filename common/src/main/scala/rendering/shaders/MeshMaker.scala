package rendering.shaders

import world2d.Hexagon

object MeshMaker {
  // standards mesh constants
  
  val indicePerSingleHexa = 8
  
  val verticePerHexa = 7 // 7 points to define the triangles in a hexagon
  val indicePerHexa = 18 // 6 explicit triangles
  
  val indexCountMax = Short.MaxValue-Short.MinValue+1 // max number of indice that can be used in a single mesh
  val hexaPerMesh = indexCountMax / verticePerHexa
}

trait MeshMaker[-H <: Hexagon] {
  
  def floatsPerVertex: Int // how many float values are stored for each vertex
  
  def floatsPerHexa = MeshMaker.verticePerHexa * floatsPerVertex // => size of a mesh per contained hexa
  
  def verticeAndIndice(h: H): (Array[Float], Array[Short])
  
  protected def reasonableVerticeAndIndice(hexas: Seq[_ <: H]): (Array[Float], Array[Short])
  
  // triangles mesh for lots of hexagons
  def verticeAndIndice(hexas: Seq[_ <: H]): Seq[(Array[Float], Array[Short])] = /*perf.perfed(s"verticeAndIndice(${hexas.size}})")*/ {
    hexas
    .grouped(MeshMaker.hexaPerMesh)
    .map(reasonableVerticeAndIndice)
    .toSeq
  }
}