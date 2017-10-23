/*
 * Apache 2.0 License
 *
 * Copyright (c) Sebastian Katzer 2017
 *
 * This file contains Original Code and/or Modifications of Original Code
 * as defined in and that are subject to the Apache License
 * Version 2.0 (the 'License'). You may not use this file except in
 * compliance with the License. Please obtain a copy of the License at
 * http://opensource.org/licenses/Apache-2.0/ and read it before using this
 * file.
 *
 * The Original Code and all software distributed under the License are
 * distributed on an 'AS IS' basis, WITHOUT WARRANTY OF ANY KIND, EITHER
 * EXPRESS OR IMPLIED, AND APPLE HEREBY DISCLAIMS ALL SUCH WARRANTIES,
 * INCLUDING WITHOUT LIMITATION, ANY WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE, QUIET ENJOYMENT OR NON-INFRINGEMENT.
 * Please see the License for the specific language governing rights and
 * limitations under the License.
 */

package de.appplant.cordova.plugin.notification;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.support.v4.app.NotificationManagerCompat;

import org.json.JSONException;
import org.json.JSONObject;

import de.appplant.cordova.plugin.badge.BadgeImpl;

import static android.os.Build.VERSION.SDK_INT;
import static android.os.Build.VERSION_CODES.O;
import static android.support.v4.app.NotificationManagerCompat.IMPORTANCE_DEFAULT;
import static de.appplant.cordova.plugin.notification.Notification.PREF_KEY;

/**
 * Central way to access all or single local notifications set by specific
 * state like triggered or scheduled. Offers shortcut ways to schedule,
 * cancel or clear local notifications.
 */
public final class Manager {

    // TODO: temporary
    static final String CHANNEL_ID = "default-channel-id";

    // TODO: temporary
    private static final CharSequence CHANNEL_NAME = "Default channel";

    // The application context
    private Context context;

    /**
     * Constructor
     *
     * @param context Application context
     */
    private Manager(Context context) {
        this.context = context;
        createDefaultChannel();
    }

    /**
     * Static method to retrieve class instance.
     *
     * @param context Application context
     */
    public static Manager getInstance(Context context) {
        return new Manager(context);
    }

    /**
     * Check if app has local notification permission.
     */
    public boolean hasPermission () {
        return getNotCompMgr().areNotificationsEnabled();
    }

    /**
     * Schedule local notification specified by request.
     *
     * @param request Set of notification options.
     * @param receiver Receiver to handle the trigger event.
     */
    public Notification schedule (Request request, Class<?> receiver) {
        Options options    = request.getOptions();
        Notification toast = new Notification(context, options);

        toast.schedule(request, receiver);

        return toast;
    }

    /**
     * TODO: temporary
     */
    private void createDefaultChannel() {
        NotificationManager mgr = getNotMgr();

        if (SDK_INT < O)
            return;

        NotificationChannel channel = mgr.getNotificationChannel(CHANNEL_ID);

        if (channel != null)
            return;

        channel = new NotificationChannel(
                CHANNEL_ID, CHANNEL_NAME, IMPORTANCE_DEFAULT);

        mgr.createNotificationChannel(channel);
    }

    // /**
    //  * Clear local notification specified by ID.
    //  *
    //  * @param id
    //  *      The notification ID
    //  * @param updates
    //  *      JSON object with notification options
    //  * @param receiver
    //  *      Receiver to handle the trigger event
    //  */
    // public Notification update (int id, JSONObject updates, Class<?> receiver) {
    //     Notification notification = get(id);

    //     if (notification == null)
    //         return null;

    //     notification.cancel();

    //     JSONObject options = mergeJSONObjects(
    //             notification.getOptions().getDict(), updates);

    //     try {
    //         options.put("updated", true);
    //     } catch (JSONException ignore) {}

    //     return schedule(options, receiver);
    // }

    // /**
    //  * Clear local notification specified by ID.
    //  *
    //  * @param id The notification ID.
    //  */
    // public Notification clear (int id) {
    //     Notification notification = get(id);

    //     if (notification != null) {
    //         notification.clear();
    //     }

    //     return notification;
    // }

    /**
     * Clear all local notifications.
     */
    public void clearAll () {
        getNotCompMgr().cancelAll();
        setBadge(0);
    }

    // /**
    //  * Clear local notification specified by ID.
    //  *
    //  * @param id
    //  *      The notification ID
    //  */
    // public Notification cancel (int id) {
    //     Notification notification = get(id);

    //     if (notification != null) {
    //         notification.cancel();
    //     }

    //     return notification;
    // }

    // /**
    //  * Cancel all local notifications.
    //  */
    // public void cancelAll () {
    //     List<Notification> notifications = getAll();

    //     for (Notification notification : notifications) {
    //         notification.cancel();
    //     }

    //     getNotCompMgr().cancelAll();
    // }

    // /**
    //  * All local notifications IDs.
    //  */
    // public List<Integer> getIds() {
    //     Set<String> keys = getPrefs().getAll().keySet();
    //     ArrayList<Integer> ids = new ArrayList<Integer>();

    //     for (String key : keys) {
    //         try {
    //             ids.add(Integer.parseInt(key));
    //         } catch (NumberFormatException e) {
    //             e.printStackTrace();
    //         }
    //     }

    //     return ids;
    // }

    // /**
    //  * All local notification IDs for given type.
    //  *
    //  * @param type
    //  *      The notification life cycle type
    //  */
    // public List<Integer> getIdsByType(Notification.Type type) {
    //     List<Notification> notifications = getAll();
    //     ArrayList<Integer> ids = new ArrayList<Integer>();

    //     for (Notification notification : notifications) {
    //         if (notification.getType() == type) {
    //             ids.add(notification.getId());
    //         }
    //     }

    //     return ids;
    // }

    // /**
    //  * List of local notifications with matching ID.
    //  *
    //  * @param ids
    //  *      Set of notification IDs
    //  */
    // public List<Notification> getByIds(List<Integer> ids) {
    //     ArrayList<Notification> notifications = new ArrayList<Notification>();

    //     for (int id : ids) {
    //         Notification notification = get(id);

    //         if (notification != null) {
    //             notifications.add(notification);
    //         }
    //     }

    //     return notifications;
    // }

    // /**
    //  * List of all local notification.
    //  */
    // public List<Notification> getAll() {
    //     return getByIds(getIds());
    // }

    // /**
    //  * List of local notifications from given type.
    //  *
    //  * @param type
    //  *      The notification life cycle type
    //  */
    // public List<Notification> getByType(Notification.Type type) {
    //     List<Notification> notifications = getAll();
    //     ArrayList<Notification> list = new ArrayList<Notification>();

    //     if (type == Notification.Type.ALL)
    //         return notifications;

    //     for (Notification notification : notifications) {
    //         if (notification.getType() == type) {
    //             list.add(notification);
    //         }
    //     }

    //     return list;
    // }

    // /**
    //  * List of local notifications with matching ID from given type.
    //  *
    //  * @param type
    //  *      The notification life cycle type
    //  * @param ids
    //  *      Set of notification IDs
    //  */
    // @SuppressWarnings("UnusedDeclaration")
    // public List<Notification> getBy(Notification.Type type, List<Integer> ids) {
    //     ArrayList<Notification> notifications = new ArrayList<Notification>();

    //     for (int id : ids) {
    //         Notification notification = get(id);

    //         if (notification != null && notification.isScheduled()) {
    //             notifications.add(notification);
    //         }
    //     }

    //     return notifications;
    // }

    // /**
    //  * If a notification with an ID exists.
    //  *
    //  * @param id
    //  *      Notification ID
    //  */
    // public boolean exist (int id) {
    //     return get(id) != null;
    // }

    // /**
    //  * If a notification with an ID and type exists.
    //  *
    //  * @param id
    //  *      Notification ID
    //  * @param type
    //  *      Notification type
    //  */
    // public boolean exist (int id, Notification.Type type) {
    //     Notification notification = get(id);

    //     return notification != null && notification.getType() == type;
    // }

    // /**
    //  * List of properties from all local notifications.
    //  */
    // public List<JSONObject> getOptions() {
    //     return getOptionsById(getIds());
    // }

    // /**
    //  * List of properties from local notifications with matching ID.
    //  *
    //  * @param ids
    //  *      Set of notification IDs
    //  */
    // public List<JSONObject> getOptionsById(List<Integer> ids) {
    //     ArrayList<JSONObject> options = new ArrayList<JSONObject>();

    //     for (int id : ids) {
    //         Notification notification = get(id);


    //         if (notification != null) {
    //             options.add(notification.getOptions().getDict());
    //         }
    //     }

    //     return options;
    // }

    // /**
    //  * List of properties from all local notifications from given type.
    //  *
    //  * @param type
    //  *      The notification life cycle type
    //  */
    // public List<JSONObject> getOptionsByType(Notification.Type type) {
    //     ArrayList<JSONObject> options = new ArrayList<JSONObject>();
    //     List<Notification> notifications = getByType(type);

    //     for (Notification notification : notifications) {
    //         options.add(notification.getOptions().getDict());
    //     }

    //     return options;
    // }

    // /**
    //  * List of properties from local notifications with matching ID from
    //  * given type.
    //  *
    //  * @param type
    //  *      The notification life cycle type
    //  * @param ids
    //  *      Set of notification IDs
    //  */
    // public List<JSONObject> getOptionsBy(Notification.Type type,
    //                                      List<Integer> ids) {

    //     if (type == Notification.Type.ALL)
    //         return getOptionsById(ids);

    //     ArrayList<JSONObject> options = new ArrayList<JSONObject>();
    //     List<Notification> notifications = getByIds(ids);

    //     for (Notification notification : notifications) {
    //         if (notification.getType() == type) {
    //             options.add(notification.getOptions().getDict());
    //         }
    //     }

    //     return options;
    // }

    /**
     * Get local notification options.
     *
     * @param id Notification ID.
     *
     * @return null if could not found.
     */
    public Options getOptions(int id) {
        SharedPreferences prefs = getPrefs();
        String toastId          = Integer.toString(id);

        if (!prefs.contains(toastId))
            return null;

        try {
            String json     = prefs.getString(toastId, null);
            JSONObject dict = new JSONObject(json);

            return new Options(context, dict);
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Get existent local notification.
     *
     * @param id Notification ID.
     *
     * @return null if could not found.
     */
    public Notification get(int id) {
        Options options = getOptions(id);

        if (options == null)
            return null;

        return new Notification(context, options);
    }

    // /**
    //  * Merge two JSON objects.
    //  *
    //  * @param obj1
    //  *      JSON object
    //  * @param obj2
    //  *      JSON object with new options
    //  */
    // private JSONObject mergeJSONObjects (JSONObject obj1, JSONObject obj2) {
    //     Iterator it = obj2.keys();

    //     while (it.hasNext()) {
    //         try {
    //             String key = (String)it.next();

    //             obj1.put(key, obj2.opt(key));
    //         } catch (JSONException e) {
    //             e.printStackTrace();
    //         }
    //     }

    //     return obj1;
    // }

    /**
     * Set the badge number of the app icon.
     *
     * @param badge The badge number.
     */
    public void setBadge (int badge) {
        if (badge == 0) {
            new BadgeImpl(context).clearBadge();
        } else {
            new BadgeImpl(context).setBadge(badge);
        }
    }

    /**
     * Shared private preferences for the application.
     */
    private SharedPreferences getPrefs () {
        return context.getSharedPreferences(PREF_KEY, Context.MODE_PRIVATE);
    }

    /**
     * Notification manager for the application.
     */
    private NotificationManager getNotMgr() {
        return (NotificationManager) context.getSystemService(
                Context.NOTIFICATION_SERVICE);
    }

    /**
     * Notification compat manager for the application.
     */
    private NotificationManagerCompat getNotCompMgr() {
        return NotificationManagerCompat.from(context);
    }

}
