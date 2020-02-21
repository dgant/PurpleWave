package Planning.Plans.Macro.Expanding

import Planning.Plans.Compound.Parallel

class MaintainMiningBases extends Parallel(
  new MaintainMiningBasesAt(1),
  new MaintainMiningBasesAt(2),
  new MaintainMiningBasesAt(3),
  new MaintainMiningBasesAt(4),
  new MaintainMiningBasesAt(5))