package Placement

object Unreserved extends PreplacementRequirement {
  override val requireBuildable: Boolean = false
  override val requireWalkable: Boolean = false
  width = 1
  height = 1
}
