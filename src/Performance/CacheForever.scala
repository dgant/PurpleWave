package Performance

import Utilities.Time.Forever

class CacheForever[T](recalculator: () => T) extends Cache[T](recalculator, Forever())
