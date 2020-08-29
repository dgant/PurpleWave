package Placement

object ReservedWalkable extends PreplacementRequirement {
  override val requireWalkable: Boolean = true
  override val requireBuildable: Boolean = false
  override val walkableAfter: Boolean = true
  override val buildableAfter: Boolean = false
  width = 1
  height = 1
}
