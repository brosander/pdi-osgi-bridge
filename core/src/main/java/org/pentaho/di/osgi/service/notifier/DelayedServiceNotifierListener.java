package org.pentaho.di.osgi.service.notifier;

import org.pentaho.di.osgi.service.lifecycle.LifecycleEvent;

/**
 * Created by bryan on 3/2/16.
 */
public interface DelayedServiceNotifierListener {
  void onRun( LifecycleEvent event, Object serviceObject );
}
