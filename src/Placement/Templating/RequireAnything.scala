package Placement.Templating

object RequireAnything extends TemplatePointRequirement {
  override val buildableBefore  : Boolean = false
  override val walkableBefore   : Boolean = false
  override val buildableAfter   : Boolean = true
  override val walkableAfter    : Boolean = true
  override val toString         : String  = "Anything"
}
