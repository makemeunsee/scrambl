package ee.uns.makeme.cfg

import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration

class AndroidConfig extends AndroidApplicationConfiguration { cfg =>
    cfg.useAccelerometer = false
    cfg.useCompass = false
    cfg.useWakelock = true
    cfg.hideStatusBar = true
}