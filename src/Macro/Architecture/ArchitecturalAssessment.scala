package Macro.Architecture

object ArchitecturalAssessment extends Enumeration {
  type ArchitecturalAssessment = Value
  val
    Accepted,
    Invalid,
    DoesntMatch,
    Unpowered,
    IsntBuildableTerrain,
    InaccessibleIsland,
    IsntGas,
    BlockedGas,
    IsntBasePosition,
    IsntLegalForTownHall,
    ViolatesPerimeter,
    IsntBuildable,
    CreepMismatch,
    ViolatesHarvesting,
    ViolatesResourceGap,
    BlockedByUnit,
    Reserved = Value
}