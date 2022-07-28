package Placement.Templating

class RequireWalkable extends TemplatePointRequirement {
  override val buildableBefore  : Boolean = false
  override val walkableBefore   : Boolean = true
  override val buildableAfter   : Boolean = false
  override val walkableAfter    : Boolean = true
  override val toString         : String  = "Walkable"
}
