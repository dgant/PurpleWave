package Planning.Plans.Placement

import Macro.Architecture.Blueprint
import Macro.Architecture.PlacementRequests.{PlacementPolicySimple, PlacementRequest}

object ChainBlueprints {

  def apply(blueprints: Blueprint*): PlacementRequest = {
    val requests = blueprints.map(new PlacementRequest(_)).toVector
    var i = 0
    while (i < requests.length - 1) {
      requests(i).child = Some(requests(i + 1))
      i += 1
    }
    requests.foreach(r => r.task = () => new PlacementPolicySimple(r))
    requests.head
  }

}
