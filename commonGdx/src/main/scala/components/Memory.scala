package components

import scala.collection.immutable.Stack

// not concurrency safe
class Memory[Action](undoComponent: Option[ToggleComponent], redoComponent: Option[ToggleComponent]) {
    
  private var undoStack = Stack.empty[Action]
  private var redoStack = Stack.empty[Action]

  def doAction(a: Action)(perform: Action => Unit) {
    undoStack = undoStack.push(a)
    undoComponent map (_.setEnabled(enabled = true))
    redoStack = Stack.empty
    redoComponent map (_.setEnabled(enabled = false))
    perform(a)
  }
  
  def undo(unperform: Action => Unit) {
    if (undoStack.nonEmpty) {
      val action = undoStack.head
      redoStack = redoStack.push(action)
      redoComponent map (_.setEnabled(enabled = true))
      undoStack = undoStack.pop
      unperform(action)
    }
    if (undoStack.isEmpty) {
      undoComponent foreach (_.setEnabled(enabled = false))
    }
  }
  
  def redo(reperform: Action => Unit) {
    if (redoStack.nonEmpty) {
      val action = redoStack.head
      undoStack = undoStack.push(action)
      undoComponent map (_.setEnabled(enabled = true))
      redoStack = redoStack.pop
      reperform(action)
    }
    if (redoStack.isEmpty) {
      redoComponent foreach (_.setEnabled(enabled = false))
    }
  }
  
  def clear() {
    undoStack = Stack.empty[Action]
    undoComponent map (_.setEnabled(enabled = false))
    redoStack = Stack.empty[Action]
    redoComponent foreach (_.setEnabled(enabled = false))
  }
  
//  def dump() {
//    val filename = s"actionsdump"
//    Try(new FileWriter(new File(filename))).map { fw =>
//      fw.write(undoStack.toSet.mkString("\r\n"))
//      fw.close()
//      println(s"dumped to $filename")
//    }
//  }
}