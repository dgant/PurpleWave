package Information.Grids.ArrayTypes

import Lifecycle.With

abstract class AbstractGridFramestamp extends AbstractGridVersioned {
 override def updateVersion() { version = With.frame }
 def frameUpdated: Int = version
}