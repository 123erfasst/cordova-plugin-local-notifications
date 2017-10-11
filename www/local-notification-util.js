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

var exec    = require('cordova/exec'),
    channel = require('cordova/channel');

// Default values
exports._defaults = {
    id:      0,
    text:    '',
    title:   '',
    sound:   'res://platform_default',
    badge:   undefined,
    data:    undefined,
    icon:    undefined,
    trigger: { type: 'calendar' },
    actions: [],
    actionGroupId: undefined,
    attachments: [],
    progressBar: false
};

// Listener
exports._listener = {};

/**
 * Merge platform specific properties into the default ones.
 *
 * @return [ Void ]
 */
exports.applyPlatformSpecificOptions = function () {
    var defaults = this._defaults;

    switch (device.platform) {
    case 'Android':
        defaults.icon       = 'res://icon';
        defaults.smallIcon  = undefined;
        defaults.sticky     = false;
        defaults.autoClear  = true;
        defaults.led        = true;
        defaults.color      = undefined;
        defaults.vibrate    = false;
        defaults.lockscreen = true;
        defaults.showWhen   = true;
        defaults.defaults   = 0;
        defaults.priority   = 0;
        break;
    }
};

/**
 * Merge custom properties with the default values.
 *
 * @param [ Object ] options Set of custom values.
 *
 * @retrun [ Object ]
 */
exports.mergeWithDefaults = function (options) {
    var defaults = this.getDefaults();

    options.text = this.getValueFor(options, 'text', 'message');
    options.data = this.getValueFor(options, 'data', 'json');

    if (defaults.hasOwnProperty('autoClear')) {
        options.autoClear = this.getValueFor(options, 'autoClear', 'autoCancel');
    }

    if (options.autoClear !== true && options.sticky) {
        options.autoClear = false;
    }

    if (defaults.hasOwnProperty('sticky')) {
        options.sticky = this.getValueFor(options, 'sticky', 'ongoing');
    }

    for (var key in defaults) {
        if (options[key] === null || options[key] === undefined) {
            if (options.hasOwnProperty(key) && ['data','sound'].indexOf(key) > -1) {
                options[key] = undefined;
            } else {
                var obj = defaults[key];
                options[key] = typeof obj === 'object' ? Object.assign({}, obj) : obj;
            }
        }
    }

    for (key in options) {
        if (!defaults.hasOwnProperty(key)) {
            // delete options[key];
            console.warn('Unknown property: ' + key);
        }
    }

    return options;
};

/**
 * Convert the passed values to their required type.
 *
 * @param [ Object ] options Properties to convert for.
 *
 * @return [ Object ] The converted property list
 */
exports.convertProperties = function (options) {
    var parseToInt = function (prop, options) {
        if (isNaN(options[prop])) {
            console.warn(prop + ' is not a number: ' + options[prop]);
            return this.getDefaults()[prop];
        } else {
            return Number(options[prop]);
        }
    };

    if (options.id) {
        options.id = parseToInt('id', options);
    }

    if (options.title) {
        options.title = options.title.toString();
    }

    if (options.text) {
        options.text  = options.text.toString();
    }

    if (options.badge) {
        options.badge = parseToInt('badge', options);
    }

    if (options.priority) {
        options.priority = parseToInt('priority', options);
    }

    if (options.defaults) {
        options.defaults = parseToInt('defaults', options);
    }

    if (typeof options.data == 'object') {
        options.data = JSON.stringify(options.data);
    }

    this.convertTrigger(options);
    this.convertActions(options);
    this.convertProgressBar(options);

    return options;
};

/**
 * Convert the passed values to their required type, modifying them
 * directly for Android and passing the converted list back for iOS.
 *
 * @param [ Map ] options Set of custom values.
 *
 * @return [ Map ] Interaction object with category & actions.
 */
exports.convertActions = function (options) {

    if (!options.actions)
        return null;

    var actions = [];

    for (var i = 0, action; i < options.actions.length; i++) {
        action = options.actions[i];

        if (!action.id) {
            console.warn(
                'Action with title ' + action.title + ' has no id and will not be added.');
            continue;
        }

        action.id = action.id.toString();

        actions.push(action);
    }

    options.category = (options.category || 'DEFAULT_GROUP').toString();
    options.actions  = actions;

    return options;
};

/**
 * Convert the passed values for the trigger to their required type.
 *
 * @param [ Map ] options Set of custom values.
 *
 * @return [ Map ] Interaction object with trigger spec.
 */
exports.convertTrigger = function (options) {
    var trigger  = options.trigger || {},
        date     = this.getValueFor(trigger, 'at', 'firstAt', 'date');

    if (!trigger.type) {
        trigger.type = trigger.center ? 'location' : 'calendar';
    }

    var isCal = trigger.type == 'calendar';

    if (isCal && !date) {
        date = this.getValueFor(options, 'at', 'firstAt', 'date');
    }

    if (isCal && !trigger.every && options.every) {
        trigger.every = options.every;
    }

    if (isCal && (trigger.in || trigger.every)) {
        date = null;
    }

    if (isCal && date) {
        date       = typeof date == 'object' ? date.getTime() : date;
        trigger.at = Math.round(date / 1000);
    }

    if (!trigger.count && device.platform == 'windows') {
        trigger.count = trigger.every ? 5 : 1;
    }

    if (trigger.every && device.platform == 'windows') {
        trigger.every = trigger.every.toString();
    }

    if (!isCal) {
        trigger.notifyOnEntry = !!trigger.notifyOnEntry;
        trigger.notifyOnExit  = trigger.notifyOnExit === true;
        trigger.radius        = trigger.radius || 5;
    }

    if (!isCal || trigger.at) {
        delete trigger.every;
    }

    delete options.every;
    delete options.at;
    delete options.firstAt;
    delete options.date;

    options.trigger = trigger;

    return options;
};

/**
 * Convert the passed values for the progressBar to their required type.
 *
 * @param [ Map ] options Set of custom values.
 *
 * @return [ Map ] Interaction object with trigger spec.
 */
exports.convertProgressBar = function (options) {
    var isAndroid = device.platform == 'Android',
        cfg       = options.progressBar;

    if (typeof cfg === 'boolean') {
        cfg = options.progressBar = { enabled: cfg };
    }

    if (typeof cfg.enabled !== 'boolean') {
        cfg.enabled = !!(cfg.value || cfg.maxValue || cfg.indeterminate !== undefined);
    }

    cfg.value = cfg.value || 0;

    if (isAndroid) {
        cfg.maxValue      = cfg.maxValue || 100;
        cfg.indeterminate = cfg.indeterminate !== undefined ? cfg.indeterminate : false;
    }

    cfg.enabled = !!cfg.enabled;

    return options;
};

/**
 * Create a callback function to get executed within a specific scope.
 *
 * @param [ Function ] fn    The function to be exec as the callback.
 * @param [ Object ]   scope The callback function's scope.
 *
 * @return [ Function ]
 */
exports.createCallbackFn = function (fn, scope) {

    if (typeof fn != 'function')
        return;

    return function () {
        fn.apply(scope || this, arguments);
    };
};

/**
 * Convert the IDs to numbers.
 *
 * @param [ Array ] ids
 *
 * @return [ Array<Number> ]
 */
exports.convertIds = function (ids) {
    var convertedIds = [];

    for (var i = 0; i < ids.length; i++) {
        convertedIds.push(Number(ids[i]));
    }

    return convertedIds;
};

/**
 * First found value for the given keys.
 *
 * @param [ Object ]         options Object with key-value properties.
 * @param [ *Array<String> ] keys    List of keys.
 *
 * @return [ Object ]
 */
exports.getValueFor = function (options) {
    var keys = Array.apply(null, arguments).slice(1);

    for (var i = 0; i < keys.length; i++) {
        var key = keys[i];

        if (options.hasOwnProperty(key)) {
            return options[key];
        }
    }
};

/**
 * Fire the event with given arguments.
 *
 * @param [ String ] event The event's name.
 * @param [ *Array]  args  The callback's arguments.
 *
 * @return [ Void]
 */
exports.fireEvent = function (event) {
    var args     = Array.apply(null, arguments).slice(1),
        listener = this._listener[event];

    if (!listener)
        return;

    for (var i = 0; i < listener.length; i++) {
        var fn    = listener[i][0],
            scope = listener[i][1];

        fn.apply(scope, args);
    }
};

/**
 * Execute the native counterpart.
 *
 * @param [ String ]  action   The name of the action.
 * @param [ Array ]   args     Array of arguments.
 * @param [ Function] callback The callback function.
 * @param [ Object ] scope     The scope for the function.
 *
 * @return [ Void ]
 */
exports.exec = function (action, args, callback, scope) {
    var fn = this.createCallbackFn(callback, scope),
        params = [];

    if (Array.isArray(args)) {
        params = args;
    } else if (args) {
        params.push(args);
    }

    exec(fn, null, 'LocalNotification', action, params);
};

// Called after 'deviceready' event
channel.deviceready.subscribe(function () {
    // Device is ready now, the listeners are registered
    // and all queued events can be executed.
    exports.exec('ready');
});

// Called before 'deviceready' event
channel.onCordovaReady.subscribe(function () {
    // Set launchDetails object
    exports.exec('launch');
    // Device plugin is ready now
    channel.onCordovaInfoReady.subscribe(function () {
        // Merge platform specifics into defaults
        exports.applyPlatformSpecificOptions();
    });
});
