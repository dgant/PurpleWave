package Types.Resources

trait Resource {
  def accept():Boolean
  def select():Array[bwapi.Unit]
}
