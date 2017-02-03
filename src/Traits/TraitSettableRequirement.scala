package Traits

import Plans.Plan

trait TraitSettableRequirement {
  var _requirement = new Plan
  def getRequirement:Plan = { _requirement }
  def setRequirement(value:Plan) = { _requirement = _requirement }
}
