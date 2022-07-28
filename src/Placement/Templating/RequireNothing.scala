package Placement.Templating

class RequireNothing extends TemplatePointRequirement {
  override val buildableBefore  : Boolean = false
  override val walkableBefore   : Boolean = false
  override val buildableAfter   : Boolean = false
  override val walkableAfter    : Boolean = false
  override val toString         : String  = "Nothing"
}
