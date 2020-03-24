package Performance.Tasks

import Information.Grids.AbstractGrid
import Lifecycle.With

abstract class GridTask[T](grid: AbstractGrid[T])           extends AbstractTask {
  override protected def onRun(): Unit = grid.update()
}

class TaskGridAltitudeBonus               extends GridTask(With.grids.altitudeBonus)
class TaskGridBuildable                   extends GridTask(With.grids.buildable)
class TaskGridBuildableTerrain            extends GridTask(With.grids.buildableTerrain)
class TaskGridBuildableTownHall           extends GridTask(With.grids.buildableTownHall)
class TaskGridCreepInitial                extends GridTask(With.grids.creepInitial)
class TaskGridCreep                       extends GridTask(With.grids.creep)
class TaskGridEnemyDetection              extends GridTask(With.grids.enemyDetection)
class TaskGridEnemyRangeAir               extends GridTask(With.grids.enemyRangeAir)
class TaskGridEnemyRangeGround            extends GridTask(With.grids.enemyRangeGround)
class TaskGridEnemyRangeAirGround         extends GridTask(With.grids.enemyRangeAirGround)
class TaskGridEnemyVision                 extends GridTask(With.grids.enemyVision)
class TaskGridEnemyVulnerabilityGround    extends GridTask(With.grids.enemyVulnerabilityGround)
class TaskGridFriendlyDetection           extends GridTask(With.grids.friendlyDetection)
class TaskGridFriendlyVision              extends GridTask(With.grids.friendlyVision)
class TaskGridMobilityBorder              extends GridTask(With.grids.mobilityAir)
class TaskGridMobilityBuildings           extends GridTask(With.grids.mobilityBuildings)
class TaskGridMobilityTerrain             extends GridTask(With.grids.mobilityTerrain)
class TaskGridPsionicStorm                extends GridTask(With.grids.psionicStorm)
class TaskGridPsi2x2and3x2                extends GridTask(With.grids.psi2Height)
class TaskGridPsi4x3                      extends GridTask(With.grids.psi3Height)
class TaskGridScoutingPathBases           extends GridTask(With.grids.scoutingPathsBases)
class TaskGridScoutingPathStartLocations  extends GridTask(With.grids.scoutingPathsStartLocations)
class TaskGridUnits                       extends GridTask(With.grids.units)
class TaskGridWalkable                    extends GridTask(With.grids.walkable)
class TaskGridWalkableTerrain             extends GridTask(With.grids.walkableTerrain)
class TaskGridUnwalkableUnits             extends GridTask(With.grids.unwalkableUnits)