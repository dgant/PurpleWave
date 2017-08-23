package Performance.TaskQueue
import Performance.Tasks.AbstractTask
import Performance.Tasks.Grids._

class TaskQueueGrids extends AbstractTaskQueue {
  
  override val tasks: Vector[AbstractTask] = Vector (
    new TaskGridAltitudeBonus               { urgency = 1   },
    new TaskGridBuildable                   { urgency = 10  },
    new TaskGridBuildableTerrain            { urgency = 10  },
    new TaskGridChokepoints                 { urgency = 1   },
    new TaskGridCreep                       { urgency = 1   },
    new TaskGridEnemyDetection              { urgency = 100 },
    new TaskGridFriendlyDetection           { urgency = 100 },
    new TaskGridFriendlyVision              { urgency = 100 },
    new TaskGridMobilityBorder              { urgency = 1   },
    new TaskGridMobilityBuildings           { urgency = 1   },
    new TaskGridMobilityTerrain             { urgency = 1   },
    new TaskGridPsi2x2and3x2                { urgency = 10  },
    new TaskGridPsi4x3                      { urgency = 10  },
    new TaskGridUnits                       { urgency = 100 },
    new TaskGridWalkable                    { urgency = 1   },
    new TaskGridWalkableTerrain             { urgency = 1   },
    new TaskGridWalkableUnits               { urgency = 1   }
  )
}
