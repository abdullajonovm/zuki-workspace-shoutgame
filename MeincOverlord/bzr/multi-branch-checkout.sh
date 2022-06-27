#!/bin/bash

# this script is called by jenkins ShoutMultiBranch task as follows
# multi-branch-checkout.sh ~/jobs/ShoutMultiBranch/workspace bzrro lightweight
# please don't mess it up by changing the expected arguments or position

if [ "$1" == "" ]; then
	echo "Must specify directory"
	exit 1
fi

if [ "$2" == "" ]; then
	BZRRO="bzr"
else
	BZRRO=$2
fi

if [ "$3" == "" ]; then
	COWEIGHT=""
else
	COWEIGHT="--$3"
fi

BZRPATH="bzr+ssh://scm.shoutgameplay.com/opt/$BZRRO"

cd $1

bzr co $COWEIGHT $BZRPATH/services/head/AccessControlService/
bzr co $COWEIGHT $BZRPATH/services/head/AccountService/
bzr co $COWEIGHT $BZRPATH/services/it2014-3/AnalyticsService/
bzr co $COWEIGHT $BZRPATH/services/it2014-3/CommunityService/
bzr co $COWEIGHT $BZRPATH/services/it2014-3/DealService/
bzr co $COWEIGHT $BZRPATH/services/it2014-3/EncryptionService/
bzr co $COWEIGHT $BZRPATH/services/it2014-3/FacebookService/
bzr co $COWEIGHT $BZRPATH/services/it2014-3/GameplayDomain/
bzr co $COWEIGHT $BZRPATH/services/head/GameplayLoadTest/
bzr co $COWEIGHT $BZRPATH/services/it2014-3/GameplayMediaService/
bzr co $COWEIGHT $BZRPATH/services/it2014-3/GamePlayService/
bzr co $COWEIGHT $BZRPATH/web/it2014-3/GameplayWebGateway/
bzr co $COWEIGHT $BZRPATH/services/head/HazelcastConfigService/
bzr co $COWEIGHT $BZRPATH/services/head/HazelcastService/
bzr co $COWEIGHT $BZRPATH/web/head/HttpConnector/
bzr co $COWEIGHT $BZRPATH/services/head/HttpConnectorService/
bzr co $COWEIGHT $BZRPATH/web/head/HttpUtils/
bzr co $COWEIGHT $BZRPATH/services/it2014-3/IdentityService/
bzr co $COWEIGHT $BZRPATH/services/it2014-3/JettyService/
bzr co $COWEIGHT $BZRPATH/services/head/LikeService/
bzr co $COWEIGHT $BZRPATH/services/head/MeincCommons/
bzr co $COWEIGHT $BZRPATH/mrsoa/head/MeincDeployPlugin/
bzr co $COWEIGHT $BZRPATH/mrsoa/head/MeincLauncher/
bzr co $COWEIGHT $BZRPATH/common/it2014-3/MeincOverlord/
bzr co $COWEIGHT $BZRPATH/services/head/Mint2Service/
bzr co $COWEIGHT $BZRPATH/mrsoa/head/MrSoaDistributedData/
bzr co $COWEIGHT $BZRPATH/mrsoa/head/MrSoaKernel/
bzr co $COWEIGHT $BZRPATH/mrsoa/head/MrSoaServiceAssembler/
bzr co $COWEIGHT $BZRPATH/services/it2014-3/NotificationService/
bzr co $COWEIGHT $BZRPATH/mrsoa/head/OsgiBoneCp/
bzr co $COWEIGHT $BZRPATH/mrsoa/head/OsgiCommonsCollections/
bzr co $COWEIGHT $BZRPATH/mrsoa/head/OsgiCommonsPool/
bzr co $COWEIGHT $BZRPATH/mrsoa/head/OsgiIbatis/
bzr co $COWEIGHT $BZRPATH/mrsoa/head/OsgiIbatis-dep/
bzr co $COWEIGHT $BZRPATH/mrsoa/head/OsgiSpringFramework/
bzr co $COWEIGHT $BZRPATH/mrsoa/head/OsgiVelocity/
bzr co $COWEIGHT $BZRPATH/services/it2014-3/PostOfficeService/
bzr co $COWEIGHT $BZRPATH/services/it2014-3/PowerupService/
bzr co $COWEIGHT $BZRPATH/services/it2014-3/PushService/
bzr co $COWEIGHT $BZRPATH/mrsoa/head/ServiceAssemblerPlugin/
bzr co $COWEIGHT $BZRPATH/mobile/head/ShoutAndroid/
bzr co $COWEIGHT $BZRPATH/mobile/head/ShoutiOS/
bzr co $COWEIGHT $BZRPATH/mrsoa/it2014-3/SpringAssemblerPlugin/
bzr co $COWEIGHT $BZRPATH/services/it2014-3/StoreService/
bzr co $COWEIGHT $BZRPATH/services/head/TeamDispatcherService/
bzr co $COWEIGHT $BZRPATH/services/it2014-3/ToolsService/
bzr co $COWEIGHT $BZRPATH/services/head/TriggerService/
bzr co $COWEIGHT $BZRPATH/services/it2014-3/VirtualWalletService/
bzr co $COWEIGHT $BZRPATH/services/it2014-3/WebCollectorService/
bzr co $COWEIGHT $BZRPATH/services/it2014-3/WebDataStoreService/

