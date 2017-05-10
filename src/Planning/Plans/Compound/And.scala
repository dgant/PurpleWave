package Planning.Plans.Compound

import Planning.Plan

class And(initialChildren:Plan*) extends Serial(initialChildren: _*) {
  
  description.set("And")
  
  override def update() { super.update() }
}