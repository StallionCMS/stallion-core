<!--
 * Stallion v1.0.0 (http://stallion.io)
 * Copyright 2016-2018 Stallion Software LLC
 * Licensed under GPL (https://github.com/StallionCMS/stallion-core/blob/master/LICENSE)
-->

<style lang="scss">
 .datetime-picker-vue {
     line-height: 47px;
     .select2-selection {
         height: 34px;
         border: 1px solid #ccc;
         margin-top: -3px;
     }
     .timezone-input {
         margin-top: -4px;
         height: 34px;
         display: inline-block;
         vertical-align: 6%;
     }
     input, .vue-datetime-picker select {
         display: inline-block;
     }

     .date-input {
         width: 100px;
     }
     .minute-input {
         width: 60px;
     }

     .time-input {
         width: 88px;
     }
     
     .times {
         height: 200px;
         overflow-y: scroll;
         border: 1px solid #CCC;
         position: absolute;
         z-index: 100;
         background-color: white;
         width: 80px;
         margin-left: 103px;
         margin-top: -7px;
         .a-time {
             width: 100%;
             padding: 4px 4px 4px 4px;
             line-height: 1.2em;
             font-size: 12px;
             cursor: pointer;
         }
         .a-time:hover {
             background-color: #E7E7E7;
         }
         .selected-time {
             background-color: #ccc;
         }
     }
     .times::-webkit-scrollbar {
         -webkit-appearance: none;
     }
     
     .times::-webkit-scrollbar:vertical {
         width: 11px;
     }
     
     .times::-webkit-scrollbar:horizontal {
         height: 11px;
     }
     
     .times::-webkit-scrollbar-thumb {
         border-radius: 8px;
         border: 2px solid white; /* should match background, can't be transparent */
         background-color: rgba(0, 0, 0, .5);
     }
     
     .times::-webkit-scrollbar-track { 
         background-color: #fff; 
         border-radius: 8px; 
     }          
     
     .timezone-input {
         width: 130px;              
         border: 0px;
         font-size: 11px;
         color: #337ab7;
         border: none;
         /* needed for Firefox: */
         overflow:hidden;
         -webkit-autofill { background: #fff !important; }
         outline: none;
         -moz-box-shadow:    0px 0px 0px 0px #FFF;
         -webkit-box-shadow: 0px 0px 0px 0px #FFF;
         box-shadow:         0px 0px 0px 0px #FFF;
     }
 }
</style>

<template>
    <div class="vue-datetime-picker datetime-picker-vue">
        <input class="form-control date-input" type="text" v-model="date" name="datepicker">
        <input class="form-control time-input" v-model="time" @click="timeInputVisible=true;scrollTimeInputVisible();" @input="timeInputVisible=false" @blur="onTimeBlur">
        <div v-if="timeInputVisible" class="times">
            <div v-for="aTime in times" @mousedown="setTimeDirectly(aTime)">
                <div :class="{'a-time': true, 'selected-time': isSelectedTime(aTime)}" >{{ aTime }}</div>
            </div>
        </div>
        <select v-show="true" class="timezone-input form-control" v-model="timezone" name="timezone">
            <option v-for="name in timezoneNames">{{ name }}</option>
        </select>
    </div>
</template>


<script>
 var timeRegularExpression = new RegExp('(\d|)\d:\d\d\s*(am|pm|AM|PM|a\\.m\\.|p\\.m\\.)');
 var times = [
         '12:00 AM',
         '12:30 AM',
         '1:00 AM',
         '1:30 AM',
         '2:00 AM',
         '2:30 AM',
         '3:00 AM',
         '3:30 AM',
         '4:00 AM',
         '4:30 AM',
         '5:00 AM',
         '5:30 AM',
         '6:00 AM',
         '6:30 AM',
         '7:00 AM',
         '7:30 AM',
         '8:00 AM',
         '8:30 AM',
         '9:00 AM',
         '9:30 AM',
         '10:00 AM',
         '10:30 AM',
         '11:00 AM',
         '11:30 AM',
         '12:00 PM',
         '12:30 PM',
         '1:00 PM',
         '1:30 PM',
         '2:00 PM',
         '2:30 PM',
         '3:00 PM',
         '3:30 PM',
         '4:00 PM',
         '4:30 PM',
         '5:00 PM',
         '5:30 PM',
         '6:00 PM',
         '6:30 PM',
         '7:00 PM',
         '7:30 PM',
         '8:00 PM',
         '8:30 PM',
         '9:00 PM',
         '9:30 PM',
         '10:00 PM',
         '10:30 PM',
         '11:00 PM',
         '11:30 PM'
 ];
 var momentFormats = [
     'YYYY-MM-DD h:mm A',
     'YYYY-MM-DD h:mmA',     
     'YYYY-MM-DD h:mm',
     'YYYY-MM-DD hh:mm A',
     'YYYY-MM-DD hh:mmA',     
     'YYYY-MM-DD hh:mm',     
     'YYYY-MM-DD H:mm',
     'YYYY-MM-DD HH:mm',               
 ];
 module.exports = {
     props: {
         value: {
             default: function() {
                 return new Date().getTime() / 1000;
             }
         }
     },
     mounted: function() {
         var self = this;
         var tagsPattern = new RegExp('\\d+:\\d\\d(am|pm|AM|PM)');
         self.tagsPattern = tagsPattern;
         
         var self = this;
         self.pikaday = new Pikaday({
             field: $(self.$el).find('[name="datepicker"]').get(0),
             onSelect: function(date) {
                 console.log('new date picked', self.pikaday.toString());
             }             
         });
         $(this.$el)
             .find('select.time-input')
               .select2({
                   tags: true,
                   createTag: function (params) {
                       // Don't offset to create a tag if there is no @ symbol
                       //if (!tagsPattern.test(params.term)) {
                           // Return null to disable tag creation
                       //    return null;
                       //}
                       return {
                           id: params.term,
                           text: params.term
                       }
                   }
               })
               .on('change', function() {
                   // Not sure why this doesn't automatically update
                   self.value = $(this).val();
                   //if (self.change) {
                   //    self.change($(this).val());
                   //}
               })
             ;

     },
     data: function() {
         var self = this;
         var tz = moment.tz.guess();
         var mils = self.value;
         var m;
         // TODO: this should be explicit
         if (mils > 0 && mils < 2467378540) {
             mils = mils * 1000;
         }
         var m = moment(mils || new Date().getTime()).tz(tz);
         var hour = m.hour();
         var time = m.format('h:mm a');
         //if (hour > 12) {
         //    time = (hour-12) + ":" + m.minute() + ' PM';
         //} else {
         //    time = hour + ":" + m.minute() + ' AM';
         //}
         return {
             timeInputVisible: false,
             date: m.format('YYYY-MM-DD'),
             time:  time,
             timezone: tz,
             timezoneNames: moment.tz.names(),
             timezoneVisible: false,
             times: times
         };
     },
     methods: {
         onTimeBlur: function() {
             var self = this;
             setTimeout(function() {
                 self.timeInputVisible = false;
             }, 20);
         },
         setTimeDirectly: function(newTime) {
             this.time = newTime;
         },
         isSelectedTime: function(aTime) {
             var aMoment = moment('01/01/01 ' + aTime, momentFormats);
             var current = this.time;
             var m = moment(new Date().getTime());
             if (!this.time) {
                 // continue
             } else if (!this.time.indexOf(':')) {
                 m = moment('01/01/01 ' + this.time + ':00', momentFormats);
             } else {
                 m = moment('01/01/01 ' + this.time, momentFormats);
             }
             if (!m.hour()) {
                 var m = moment(new Date().getTime(), momentFormats);
             }
             if (m.hour() === aMoment.hour()) {
                 if (aMoment.minute() < 29 && m.minute() < 29) {
                     return true;
                 } else if (aMoment.minute() >= 30 && m.minute() >= 30) {
                     return true;
                 }
             }
         },
         scrollTimeInputVisible: function() {
             var self = this;
             setTimeout(function() {
                 var $parentDiv = $(self.$el).find('.times');
                 var $innerListItem = $parentDiv.find('.selected-time');
                 $parentDiv.scrollTop($parentDiv.scrollTop() + $innerListItem.position().top
                     - $parentDiv.height()/2 + $innerListItem.height()/2);
             }, 10);
         },
         timezoneClick: function() {
             var self = this;
             this.timezoneVisible = true;
             Vue.nextTick(function() {
                 $(self.$el).find('.timezone-input').focus();
             });
             setTimeout(function() {
                 function triggerMouseEvent (node, eventType) {
                     var clickEvent = document.createEvent ('MouseEvents');
                     clickEvent.initEvent (eventType, true, true);
                     node.dispatchEvent (clickEvent);
                 }
                 //triggerMouseEvent($(self.$el).find('.timezone-input').get(0), 'mousedown');
                 $(self.$el).find('.timezone-input').get(0).click();
                 triggerMouseEvent($(self.$el).find('.timezone-input').get(0), 'click');
             }, 100);
         },
         changed: function(a, b) {
             var self = this;
             var datestamp = self.date + ' ' + self.time;
             var m = moment.tz(datestamp, momentFormats, self.timezone);
             //this.value = m.valueOf() / 1000;
             this.$emit('input', m.valueOf() / 1000);
         }
     },
     watch: {
         'time': 'changed',
         'timezone': 'changed',
         'date': 'changed'
     }
     
 };

</script>
