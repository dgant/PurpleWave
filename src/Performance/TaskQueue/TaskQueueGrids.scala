package Performance.TaskQueue
import Performance.Tasks.AbstractTask
import Performance.Tasks.Grids._

class TaskQueueGrids extends AbstractTaskQueue {
  
  override val tasks: Vector[AbstractTask] = Vector (
    new TaskGridPsi2x2and3x2                { urgency = 2  },
    new TaskGridPsi4x3                      { urgency = 2  },
    new TaskGridWalkable                    { urgency = 1  },
    new TaskGridWalkableTerrain             { urgency = 1  },
    new TaskGridWalkableUnits               { urgency = 1  },
    new TaskGridBuildable                   { urgency = 2  },
    new TaskGridBuildableTerrain            { urgency = 2  },
    new TaskGridUnits                       { urgency = 10 },
    new TaskGridAltitudeBonus               { urgency = 10 },
    new TaskGridEnemyDetection              { urgency = 10 },
    new TaskGridEnemyVision                 { urgency = 10 },
    new TaskGridMobility                    { urgency = 1  },
    new TaskGridDpsEnemyGroundConcussive    { urgency = 10 },
    new TaskGridDpsEnemyGroundExplosive     { urgency = 10 },
    new TaskGridDpsEnemyGroundNormal        { urgency = 10 },
    new TaskGridDpsEnemyAirConcussive       { urgency = 10 },
    new TaskGridDpsEnemyAirExplosive        { urgency = 10 },
    new TaskGridDpsEnemyAirNormal           { urgency = 10 }
  )
}
