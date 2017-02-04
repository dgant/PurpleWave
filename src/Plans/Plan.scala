package Plans

import Traits.TraitSettableDescription

class Plan
extends TraitSettableDescription {
  
  def isComplete():Boolean = { false }
  def children(): Iterable[Plan] = { List.empty }
  def onFrame() = {}
  def drawOverlay() = { }
}