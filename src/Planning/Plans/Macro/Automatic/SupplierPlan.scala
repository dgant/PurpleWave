package Planning.Plans.Macro.Automatic

import Lifecycle.With
import Planning.Plan

class SupplierPlan extends Plan {
  override def onUpdate(): Unit = {
    With.supplier.prioritize()
  }
}
