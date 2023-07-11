package Mathematics.Physics

import Debugging.Visualizations.ForceLabel

import scala.collection.mutable

class ForceMap extends mutable.HashMap[ForceLabel, Force](){
  override def default(key: ForceLabel): Force = Forces.None
  def forces: Iterable[Force] = values
  def sum: Force = ForceMath.sum(forces)
}
