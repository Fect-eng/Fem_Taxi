package pe.com.android.femtaxi.providers;

import com.google.android.gms.tasks.Task;
import com.google.firebase.messaging.FirebaseMessaging;

public class TopicProvider {
    FirebaseMessaging firebaseMessaging;

    public TopicProvider() {
        firebaseMessaging = FirebaseMessaging.getInstance();
    }

    public Task<Void> registerTopic(String topic) {
        return firebaseMessaging.subscribeToTopic(topic.replace(" ", ""));
    }

}
