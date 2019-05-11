package com.scttshop.api.Configuration;

//@Configuration
//public class HazelcastCacheConfig {
//
//    @Bean
//    public CacheManager cacheManager() {
//        return new HazelcastCacheManager(hazelcastInstance());
//    }
//
//    @Bean
//    public HazelcastInstance hazelcastInstance() {
//        HazelcastInstance instance = Hazelcast.newHazelcastInstance(hazelcastConfig());
//        return instance;
//    }
//
//    @Bean
//    public Config hazelcastConfig() {
//        return new Config().setInstanceName("hazelcast-instance")
//                .addMapConfig(productMapConfig()).addMapConfig(categoryMapConfig());
//
//    }
//
//    private MapConfig categoryMapConfig(){
//        return new MapConfig().setName("categoryCache")
//                .setMaxSizeConfig(new MaxSizeConfig(300, MaxSizeConfig.MaxSizePolicy.FREE_HEAP_SIZE))
//                .setEvictionPolicy(EvictionPolicy.LFU).setTimeToLiveSeconds(-1);
//    }
//
//    private MapConfig productMapConfig(){
//        return new MapConfig().setName("productCache")
//                .setMaxSizeConfig(new MaxSizeConfig(300, MaxSizeConfig.MaxSizePolicy.FREE_HEAP_SIZE))
//                .setEvictionPolicy(EvictionPolicy.LFU).setTimeToLiveSeconds(-1);
//    }
//
//}