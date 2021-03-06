/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2016 by Pentaho : http://www.pentaho.com
 *
 *******************************************************************************
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 ******************************************************************************/

package org.pentaho.di.osgi.service.notifier;

import org.junit.Before;
import org.junit.Test;
import org.pentaho.di.osgi.OSGIPluginTracker;
import org.pentaho.di.osgi.service.lifecycle.LifecycleEvent;
import org.pentaho.di.osgi.service.lifecycle.OSGIServiceLifecycleListener;
import org.pentaho.osgi.api.BeanFactory;
import org.pentaho.osgi.api.ProxyUnwrapper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static org.mockito.Mockito.*;

/**
 * Created by bryan on 8/18/14.
 */
public class DelayedServiceNotifierTest {
  private Map<Class, List<OSGIServiceLifecycleListener>> instanceListeners;
  private ScheduledExecutorService scheduler;
  private OSGIPluginTracker osgiPluginTracker;
  private Object serviceObject;
  private Class<?> clazz;
  private ProxyUnwrapper proxyUnwrapper;
  private DelayedServiceNotifierListener delayedServiceNotifierListener;

  @Before
  public void setup() {
    instanceListeners = new HashMap<Class, List<OSGIServiceLifecycleListener>>();
    scheduler = mock( ScheduledExecutorService.class );
    osgiPluginTracker = mock( OSGIPluginTracker.class );
    serviceObject = mock( Object.class );
    proxyUnwrapper = mock( ProxyUnwrapper.class );
    delayedServiceNotifierListener = mock( DelayedServiceNotifierListener.class );
    clazz = Map.class;
  }

  @Test
  public void testFactoryEqualNotNullStarting() {
    DelayedServiceNotifier delayedServiceNotifier =
      new DelayedServiceNotifier( osgiPluginTracker, clazz, LifecycleEvent.START, serviceObject,
        instanceListeners, scheduler, delayedServiceNotifierListener );
    OSGIServiceLifecycleListener listener = mock( OSGIServiceLifecycleListener.class );
    List<OSGIServiceLifecycleListener> listeners =
      new ArrayList<OSGIServiceLifecycleListener>( Arrays.asList( listener ) );
    instanceListeners.put( clazz, listeners );
    BeanFactory beanFactory = mock( BeanFactory.class );
    when( osgiPluginTracker.findOrCreateBeanFactoryFor( serviceObject ) ).thenReturn( beanFactory );
    when( osgiPluginTracker.getProxyUnwrapper() ).thenReturn( proxyUnwrapper );
    delayedServiceNotifier.run();
    verify( listener ).pluginAdded( serviceObject );
  }

  @Test
  public void testFactoryEqualNotNullStopping() {
    DelayedServiceNotifier delayedServiceNotifier =
      new DelayedServiceNotifier( osgiPluginTracker, clazz, LifecycleEvent.STOP, serviceObject,
        instanceListeners, scheduler, delayedServiceNotifierListener );
    OSGIServiceLifecycleListener listener = mock( OSGIServiceLifecycleListener.class );
    List<OSGIServiceLifecycleListener> listeners =
      new ArrayList<OSGIServiceLifecycleListener>( Arrays.asList( listener ) );
    instanceListeners.put( clazz, listeners );
    BeanFactory beanFactory = mock( BeanFactory.class );
    when( osgiPluginTracker.findOrCreateBeanFactoryFor( serviceObject ) ).thenReturn( beanFactory );
    delayedServiceNotifier.run();
    verify( listener ).pluginRemoved( serviceObject );
  }

  @Test
  public void testFactoryEqualNotNullModified() {
    DelayedServiceNotifier delayedServiceNotifier =
      new DelayedServiceNotifier( osgiPluginTracker, clazz, LifecycleEvent.MODIFY, serviceObject,
        instanceListeners, scheduler, delayedServiceNotifierListener );
    OSGIServiceLifecycleListener listener = mock( OSGIServiceLifecycleListener.class );
    List<OSGIServiceLifecycleListener> listeners =
      new ArrayList<OSGIServiceLifecycleListener>( Arrays.asList( listener ) );
    instanceListeners.put( clazz, listeners );
    BeanFactory beanFactory = mock( BeanFactory.class );
    when( osgiPluginTracker.findOrCreateBeanFactoryFor( serviceObject ) ).thenReturn( beanFactory );
    when( osgiPluginTracker.getProxyUnwrapper() ).thenReturn( proxyUnwrapper );
    delayedServiceNotifier.run();
    verify( listener ).pluginChanged( serviceObject );
  }

  @Test
  public void testAllEventTypesLegal() {
    // This test will catch an unhandled event type
    for ( LifecycleEvent eventType : LifecycleEvent.values() ) {
      DelayedServiceNotifier delayedServiceNotifier =
        new DelayedServiceNotifier( osgiPluginTracker, clazz, eventType, serviceObject,
          instanceListeners, scheduler, delayedServiceNotifierListener );
      OSGIServiceLifecycleListener listener = mock( OSGIServiceLifecycleListener.class );
      List<OSGIServiceLifecycleListener> listeners =
        new ArrayList<OSGIServiceLifecycleListener>( Arrays.asList( listener ) );
      instanceListeners.put( clazz, listeners );
      BeanFactory beanFactory = mock( BeanFactory.class );
      when( osgiPluginTracker.findOrCreateBeanFactoryFor( serviceObject ) ).thenReturn( beanFactory );
      delayedServiceNotifier.run();
    }
  }

  @Test
  public void testFactoryEqualNull() {
    DelayedServiceNotifier delayedServiceNotifier =
      new DelayedServiceNotifier( osgiPluginTracker, clazz, LifecycleEvent.START, serviceObject,
        instanceListeners, scheduler, delayedServiceNotifierListener );
    when( osgiPluginTracker.findOrCreateBeanFactoryFor( serviceObject ) ).thenReturn( null );
    delayedServiceNotifier.run();
    verify( scheduler ).schedule( delayedServiceNotifier, 100, TimeUnit.MILLISECONDS );
  }

  @Test
  public void testDelayedServiceNotifierListener() {
    DelayedServiceNotifier delayedServiceNotifier =
      new DelayedServiceNotifier( osgiPluginTracker, clazz, LifecycleEvent.START, serviceObject,
        instanceListeners, scheduler, delayedServiceNotifierListener );
    BeanFactory beanFactory = mock( BeanFactory.class );
    when( osgiPluginTracker.findOrCreateBeanFactoryFor( serviceObject ) ).thenReturn( beanFactory );
    when( osgiPluginTracker.getProxyUnwrapper() ).thenReturn( mock( ProxyUnwrapper.class ) );
    delayedServiceNotifier.run();
    verify( delayedServiceNotifierListener ).onRun( LifecycleEvent.START, serviceObject );
  }

  @Test
  public void testDelayedServiceNotifierListenerNull() {
    DelayedServiceNotifier delayedServiceNotifier =
      new DelayedServiceNotifier( osgiPluginTracker, clazz, LifecycleEvent.START, serviceObject,
        instanceListeners, scheduler, null );
    OSGIServiceLifecycleListener osgiServiceLifecycleListener = mock( OSGIServiceLifecycleListener.class );
    instanceListeners.put( clazz, new ArrayList<>( Arrays.asList( osgiServiceLifecycleListener ) ) );
    BeanFactory beanFactory = mock( BeanFactory.class );
    when( osgiPluginTracker.findOrCreateBeanFactoryFor( serviceObject ) ).thenReturn( beanFactory );
    when( osgiPluginTracker.getProxyUnwrapper() ).thenReturn( mock( ProxyUnwrapper.class ) );
    delayedServiceNotifier.run();
    verify( osgiServiceLifecycleListener ).pluginAdded( serviceObject );
  }
}
