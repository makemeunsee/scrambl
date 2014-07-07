package rendering.shaders

import world2d.Hexagon
import com.badlogic.gdx.graphics.Mesh

trait GdxShaderModule[-H <: Hexagon] extends GdxShader with GdxMeshMaker[H] {
  def createMesh(h: H): Mesh = {
    val mesh = new Mesh(true,
      MeshMaker.verticePerHexa,
      MeshMaker.indicePerSingleHexa,
      attributes: _*)
    val (vertice, indice) = verticeAndIndice(h)
    mesh.setVertices(vertice)
    mesh.setIndices(indice)
    mesh
  }
  
  def createMeshes(hexas: Seq[_ <: H]): Seq[Mesh] = {
    verticeAndIndice(hexas)
    .map { case (vertice, indice) =>
      new Mesh(true, vertice.size / floatsPerVertex, indice.size, attributes: _*)
      .setVertices(vertice)
      .setIndices(indice)
    } 
  }
}