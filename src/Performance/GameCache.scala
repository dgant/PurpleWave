package Performance

import Lifecycle.With

class GameCache[TValue](getValue: () => TValue) extends KeyedCache[TValue, Long](getValue, () => With.id)