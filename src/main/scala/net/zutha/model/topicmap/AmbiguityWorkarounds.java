package net.zutha.model.topicmap;

import de.topicmapslab.majortom.model.index.ITransitiveTypeInstanceIndex;
import org.tmapi.core.Topic;
import org.tmapi.core.TopicMap;

import java.util.Collection;
import java.util.Set;

public class AmbiguityWorkarounds {

    public static Set<Topic> getAllTopics(TopicMap tm){
        return tm.getTopics();
    }
    public static Collection<Topic> getTopics(ITransitiveTypeInstanceIndex index, Topic tt){
        return index.getTopics(tt);
    }
}
