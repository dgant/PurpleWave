package Debugging.Visualizations

import Mathematics.Physics.{Force, ForceMath}

import scala.collection.mutable

class ForceMap extends mutable.HashMap[ForceLabel, Force](){
  override def default(key: ForceLabel): Force = new Force
  def forces: Iterable[Force] = values
  def sum: Force = ForceMath.sum(forces)
}
