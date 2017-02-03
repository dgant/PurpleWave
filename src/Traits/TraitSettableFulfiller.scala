package Traits

import Plans.Plan

trait TraitSettableFulfiller {
  var _fulfiller = new Plan
  def getFulfiller:Plan = { _fulfiller }
  def setFulfiller(value:Plan) = { _fulfiller = _fulfiller }
}
