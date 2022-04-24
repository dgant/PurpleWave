package Placement.Architecture

object ArchitecturalAssessment extends Enumeration {
  type ArchitecturalAssessment = Value
  val
    Accepted,
    Invalid,
    Unpowered,
    IsntBuildableTerrain,
    InaccessibleIsland,
    IsntGas,
    BlockedGas,
    IsntBasePosition,
    IsntLegalForTownHall,
    IsntBuildable,
    CreepMismatch,
    ViolatesResourceGap,
    BlockedByUnit
    = Value
}