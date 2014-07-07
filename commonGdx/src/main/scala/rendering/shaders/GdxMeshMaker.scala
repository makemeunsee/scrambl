package rendering.shaders

import rendering.Color
import world2d.Hexagon
import com.badlogic.gdx.graphics.Mesh
import com.badlogic.gdx.graphics.VertexAttribute

import MeshMaker._

trait GdxMeshMaker[-H <: Hexagon] extends MeshMaker[H] {
  
  protected def attributes: Seq[VertexAttribute]

  def createMesh(h: H): Mesh
  
  def createMeshes(hexas: Seq[_ <: H]): Seq[Mesh]
}