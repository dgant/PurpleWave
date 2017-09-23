package Planning.Plans.Compound

import Planning.Plan

class And(initialChildren: Plan*) extends Serial(initialChildren: _*) {
  
  override def toString: String = "(" + children.get.map(_.toString).mkString(" AND ") + ")"
}