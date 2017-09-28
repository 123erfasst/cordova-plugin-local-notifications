﻿/*
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

using Windows.Data.Xml.Dom;

namespace LocalNotificationProxy.LocalNotification
{
    public sealed class Trigger
    {
        /// <summary>
        /// Gets trigger type.
        /// </summary>
        public string Type { get; } = "calendar";

        /// <summary>
        /// Gets or sets trigger date.
        /// </summary>
        public long At { get; set; } = 0;

        /// <summary>
        /// Gets or sets relative trigger date in seconds from now.
        /// </summary>
        public long In { get; set; } = 0;

        /// <summary>
        /// Gets or sets trigger count.
        /// </summary>
        public int Count { get; set; } = 1;

        /// <summary>
        /// Gets trigger occurrence.
        /// </summary>
        public int Occurrence { get; internal set; } = 1;

        /// <summary>
        /// Gets or sets trigger interval.
        /// </summary>
        public string Every { get; set; }

        /// <summary>
        /// Deserializes the XML string into an instance of Trigger.
        /// </summary>
        /// <param name="xml">The serialized instance of Options as an xml string.</param>
        /// <returns>An instance where all properties have been assigned.</returns>
        internal static Trigger Parse(string xml)
        {
            var doc = new XmlDocument();
            doc.LoadXml(xml);

            var trigger = new Trigger();
            var node = doc.DocumentElement;

            trigger.At = long.Parse(node.GetAttribute("at"));
            trigger.In = long.Parse(node.GetAttribute("in"));
            trigger.Count = int.Parse(node.GetAttribute("count"));
            trigger.Occurrence = int.Parse(node.GetAttribute("occurrence"));

            if (node.GetAttributeNode("every") != null)
            {
                trigger.Every = node.GetAttribute("every");
            }

            return trigger;
        }

        /// <summary>
        /// Gets the instance as an serialized xml element.
        /// </summary>
        /// <returns>Element with all property values set as attributes.</returns>
        internal string GetXml()
        {
            var node = new XmlDocument().CreateElement("trigger");

            node.SetAttribute("at", this.At.ToString());
            node.SetAttribute("in", this.In.ToString());
            node.SetAttribute("count", this.Count.ToString());
            node.SetAttribute("occurrence", this.Occurrence.ToString());

            if (this.Every != null)
            {
                node.SetAttribute("every", this.Every);
            }

            return node.GetXml();
        }
    }
}
