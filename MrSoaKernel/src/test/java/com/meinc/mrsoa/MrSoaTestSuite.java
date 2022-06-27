package com.meinc.mrsoa;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import com.meinc.mrsoa.monitor.MrSoaMonitorTest;
import com.meinc.mrsoa.monitor.ServerMonitorTest;
import com.meinc.mrsoa.net.TcpHelperNioTest;
import com.meinc.mrsoa.net.TcpHelperTest;
import com.meinc.mrsoa.net.inbound.AckerTest;
import com.meinc.mrsoa.net.inbound.MrSoaReceiverTest;
import com.meinc.mrsoa.net.inbound.MrSoaResponderFactoryTest;
import com.meinc.mrsoa.net.inbound.MrSoaResponderPoolTest;
import com.meinc.mrsoa.net.inbound.MrSoaResponderTest;
import com.meinc.mrsoa.net.inbound.ServiceRegistryTest;
import com.meinc.mrsoa.net.outbound.MrSoaConnectionPoolTest;
import com.meinc.mrsoa.net.outbound.MrSoaRequesterTest;
import com.meinc.mrsoa.service.ServiceEndpointTest;

@RunWith(Suite.class)
@SuiteClasses({
  TcpHelperTest.class,
  TcpHelperNioTest.class,
  AckerTest.class,
  MrSoaRequesterTest.class,
  MrSoaReceiverTest.class,
  MrSoaResponderFactoryTest.class,
  MrSoaResponderPoolTest.class,
  MrSoaResponderTest.class,
  ServiceRegistryTest.class,
  MrSoaConnectionPoolTest.class,
  MrSoaMonitorTest.class,
  ServerMonitorTest.class,
  ServiceEndpointTest.class
})
public class MrSoaTestSuite {

}
