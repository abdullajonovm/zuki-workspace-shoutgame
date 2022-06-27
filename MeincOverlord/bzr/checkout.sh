#!/bin/bash

URI="bzr+ssh://scm.shoutgameplay.com/opt/bzr/shout/game"

#MrSoa
bzr co $URI/MeincCommons
bzr co $URI/MeincLauncher
#bzr co $URI/MeincOverlord
bzr co $URI/MrSoaDistributedData
bzr co $URI/MrSoaKernel
bzr co $URI/MrSoaServiceAssembler
bzr co $URI/OsgiBoneCp
bzr co $URI/OsgiCommonsCollections
bzr co $URI/OsgiCommonsPool
bzr co $URI/OsgiIbatis
bzr co $URI/OsgiIbatis-dep
bzr co $URI/OsgiSpringFramework
bzr co $URI/OsgiVelocity
bzr co $URI/ServiceAssemblerPlugin
bzr co $URI/SpringAssemblerPlugin

#Supporting Services
bzr co $URI/EncryptionService
bzr co $URI/FacebookService
bzr co $URI/GameplayDomain
bzr co $URI/HazelcastService
bzr co $URI/HazelcastConfigService
bzr co $URI/HttpConnectorService
bzr co $URI/HttpUtils
bzr co $URI/IdentityService
bzr co $URI/MrSoaSupport
bzr co $URI/NotificationService
bzr co $URI/PostOfficeService
bzr co $URI/PushService
bzr co $URI/StoreService
bzr co $URI/SyncService
bzr co $URI/TriggerService
bzr co $URI/WebCollectorService
bzr co $URI/WebDataStoreService

#Game Services
bzr co $URI/ShoutContestAwardService
bzr co $URI/ShoutContestService
bzr co $URI/SnowyowlService

#Static Content
bzr co $URI/SnowyOwlAdmin
bzr co $URI/SnowyOwlHtml5
bzr co $URI/SnowyowlStaticContent
bzr co $URI/UrlShortenerService
