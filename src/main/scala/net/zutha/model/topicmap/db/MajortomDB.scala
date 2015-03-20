package net.zutha.model.topicmap.db

import de.topicmapslab.majortom.model.core.ITopicMapSystem
import org.tmapi.core.TopicMapSystemFactory
import de.topicmapslab.majortom.store.TopicMapStoreProperty
import de.topicmapslab.majortom.util.FeatureStrings


trait MajortomDB {
//  val rdbms = "de.topicmapslab.majortom.database.store.JdbcTopicMapStore"
//  val queued = "de.topicmapslab.majortom.queued.store.QueuedTopicMapStore"
  val inMemory = "de.topicmapslab.majortom.inmemory.store.InMemoryTopicMapStore"
  val redis = "de.topicmapslab.majortom.redis.store.RedisTopicMapStore"

  val useStore = redis

//  var redisHost = "redis.zutha.net"
  var redisHost = "localhost"
  var redisPort = "6379"
  var redisPass = "ooEE8u32u8oO!3A!jkh9@EUhq$XXUEE88E"
  
  def makeTopicMapSystem: ITopicMapSystem = {
    val factory = TopicMapSystemFactory.newInstance();
    factory.setProperty(TopicMapStoreProperty.TOPICMAPSTORE_CLASS,useStore)
    //JDBC properties
    factory.setProperty("de.topicmapslab.majortom.jdbc.host", "localhost")
    factory.setProperty("de.topicmapslab.majortom.jdbc.database", "majortom")
    factory.setProperty("de.topicmapslab.majortom.jdbc.user", "postgres")
    factory.setProperty("de.topicmapslab.majortom.jdbc.password", "postgres")
    factory.setProperty("de.topicmapslab.majortom.jdbc.dialect", "POSTGRESQL99")

    //Redis properties
    factory.setProperty("de.topicmapslab.majortom.redis.host", redisHost)
    factory.setProperty("de.topicmapslab.majortom.redis.port", redisPort)
    factory.setProperty("de.topicmapslab.majortom.redis.database", "0")
    //factory.setProperty("de.topicmapslab.majortom.redis.password", redisPass)


    //Features
    factory.setFeature(FeatureStrings.AUTOMATIC_MERGING,true)
    factory.setFeature(FeatureStrings.MERGING_SUPPORT_FEATURE_BY_TOPIC_NAME,true)
    //factory.setFeature(FeatureStrings.SUPPORT_HISTORY, true);

    factory.newTopicMapSystem().asInstanceOf[ITopicMapSystem]
  }
}
