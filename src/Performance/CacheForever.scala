package Performance

import Utilities.Forever

class CacheForever[T](recalculator: () => T) extends Cache[T](recalculator, Forever())
