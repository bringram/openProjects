/**
* {@link http://api.jquery.com/}
* @namespace jQuery
*/

(function( $ ){
    //this filter is applied when reading settings.
    var slimFilter = {
        extract : function(type, obj){
            if(type.type === 'Radio' || type.type === 'Checkbox'){
                if(type.checked){
                    return obj.value;
                }
            }else if(type.type === 'Fieldset'){
                return type.elements;
            }else{
                return obj.value;
            }
        },
        fill : function(type, obj){
           var json = type.toJSON();
           switch(type.type){
               case 'Checkbox':
               case 'Radio':
                   json.checked = (type.value === obj);
                   json.value = obj;
                   break;
               case 'Fieldset':
                   //console.log('fieldset',name,value,target);
                   break;
               default:
                   json.value = obj;
           }
           return json;
       }
    };

    /**
     * Simplifies adding to an object.  If an object key already exists, change value to an array
     * and add the value to the array.
     */
    var _add = function(obj, type, value){
        if(typeof value === 'undefined' || value === null) return;

        var key = type.name;

        if(typeof obj[key] === 'undefined'){
            obj[key] = value;
        }else if($.isArray(obj[key])){
            obj[key].push(value);
        }else{
            var temp = obj[key];
            obj[key] = [];
            obj[key].push(temp);
            obj[key].push(value);
        }
    };

    /**
     * Supported form elements
     * @namespace Types
     */
    var Types = {
        //text, password, radio, checkbox, html5 types
        /**
         * Represents an HTML &lt;input type="text"&gt; element.
         * @constructor
         * @memberof Types
         * @extends TypeBase
         * @param $el {jQuery} A jquery element, should be an HTML input element.
         * @param name {String} The name attribute of $el.
         * @param value {Object} The value attribute of $el.
         */
        Text:function($el, name, value){this.$el=$el;this.name=name;this.value=value;this.type='Text';},
        /**
         * Represents an HTML checkbox representation
         * @constructor
         * @memberof Types
         * @extends TypeBase
         * @param $el {jQuery} A jquery element, should be an HTML &lt;input type="checkbox"&gt; element.
         * @param name {String} The name attribute of $el.
         * @param value {Object} The value attribute of $el.
         * @param checked {boolean} Boolean representing whether $el is checked or not.
         */
        Checkbox:function($el, name, value, checked){this.$el=$el;this.name=name;this.value=value;this.checked=checked;this.type='Checkbox'},
        /**
         * Represents an HTML radio representation
         * @constructor
         * @memberof Types
         * @extends TypeBase
         * @param $el {jQuery} A jquery element, should be an HTML &lt;input type="radio"&gt; element.
         * @param name {String} The name attribute of $el.
         * @param value {Object} The value attribute of $el.
         * @param checked {boolean} Boolean representing whether $el is checked or not.
         */
        Radio:function($el, name, value, checked){this.$el=$el;this.name=name;this.value=value;this.checked=checked;this.type='Radio';},
        /**
         * Represents an HTML &lt;select&gt; element.
         * @constructor
         * @memberof Types
         * @extends TypeBase
         * @param $el {jQuery} A jquery element, should be an HTML select element.
         * @param name {String} The name attribute of $el.
         * @param value {Object} The value attribute of $el.
         */
        Select:function($el, name, value){this.$el=$el;this.name=name;this.value=value;this.type='Select';},
        /**
         * Represents an HTML &lt;select multiple&gt; element.
         * @constructor
         * @memberof Types
         * @extends TypeBase
         * @param $el {jQuery} A jquery element, should be an HTML &lt;select multiple&gt; element.
         * @param name {String} The name attribute of $el.
         * @param value {Object} The value attribute of $el.
         */
        SelectMulti:function($el, name, value){this.$el=$el;this.name=name;this.value=value;this.type='SelectMulti';},
        /**
         * Represents an HTML &lt;textarea&gt; element.
         * @constructor
         * @memberof Types
         * @extends TypeBase
         * @param $el {jQuery} A jquery element, should be an HTML &lt;textarea&gt; element.
         * @param name {String} The name attribute of $el.
         * @param value {Object} The value attribute of $el.
         */
        TextArea:function($el, name, value){this.$el=$el;this.name=name;this.value=value;this.type='TextArea';},
        /**
         * Represents an HTML &lt;button&gt; element or an &lt;input type="button"&gt; element.
         * @constructor
         * @memberof Types
         * @extends TypeBase
         * @param $el {jQuery} A jquery element, should be an HTML button or input of type &lt;button&gt; element.
         * @param name {String} The name attribute of $el.
         * @param value {Object} The value attribute of $el.
         */
        Button:function($el, name, value){this.$el=$el;this.name=name;this.value=value;this.type='Button';},
        /**
         * @constructor
         * @memberof Types
         * @extends TypeBase
         * @param $el {jQuery} A jquery element, should be an HTML &lt;datalist&gt; element.
         * @param name {String} The name attribute of $el.
         * @param value {Object} The value attribute of $el.
         */
        DataList:function($el, name, value){this.$el=$el;this.name=name;this.value=value;this.type='DataList';},
        /**
         * @constructor
         * @memberof Types
         */
        KeyGen:function($el, name, value){this.$el=$el;this.name=name;this.value=value;this.type='KeyGen'},

        /**
         * A special type that allows for grouping.  Used for organizational purposes.
         * @constructor
         * @memberof Types
         * @extends TypeBase
         * @param $el {jQuery} A jquery element, shoudl be an HTML &lt;fieldset&gt; element.
         * @param name {string} The name attribute of $el.
         */
        Fieldset:function($el, name){this.$el=$el;this.name=name;this.type='Fieldset';}
    };

    /**
     * This is the base class that all Types objects extend.
     * @class TypeBase
     */
    var TypeBase = function(){
        /**
         * Returns the elements name attribute.
         * @method getName
         * @instance
         * @memberof TypeBase
         * @return {String} The form elements name attribute value.
         */
        this.getName = function(){return this.name};
        /**
         * @method getValue
         * @instance
         * @memberof TypeBase
         * @return {Object} The form elements value attribute value.
         */
        this.getValue = function(){return this.value};
        /**
         * Gets the type of the object.
         * @method getType
         * @instance
         * @memberof TypeBase
         * @return {String} The string name of the Types object.
         */
        this.getType = function(){return this.prototype.constructor};
        /**
         * @method equals Checks if two {@link Types} are equal.
         * @memberof TypeBase
         * @instance
         * @method equals
         * @param type {Types} A Types object.
         * @return {boolean} True if they are equivalent, false otherwise.
         */
        this.equals = function(type){return (type && this.type === type.type && this.name === type.name)};
        /**
         * Get the serializable JSON representation of the Types object.
         * @memberof TypeBase
         * @instance
         * @method toJSON
         * @return {Object} A json representation of this Type.
         */
        this.toJSON = function(){
            var result = {name : this.name, value : this.value, type : this.type}
            if(typeof this.checked !== 'undefined') result.checked = this.checked; //for radios and checkboxes
            if(typeof this.elements !== 'undefined') result.elements = this.elements; //for fieldset
            return result;
        };
        /**
         * Update the inner representation of Types object from a given JSON represetation.  See {@link TypeBase#toJSON}.
         * @instance
         * @memberof TypeBase
         * @method updateValue
         * @param json {Object} A JSON representation of a Types object.  See {@link TypeBase#toJSON}.
         */
        this.updateValue = function(json){
            if(!this.beforeUpdate()){
                if(console) console.log("Skipping update for "+this.getName());
                return;
            }
            this.$el.val(json.value);
            this.$el.trigger("update");
            this.afterUpdate();
        };

        /**
         * Called before {@link #updateValue()}.  Fieldset does not call this.
         * @return {Boolean} True to continue with the update.  False to cancel the update.
         */
        this.beforeUpdate = function(){return true;};

        /**
         * Called after update.  This is only called if the form value is updated.  It is possible that
         * {@link #beforeUpdate()} returned false, which would prevent this from being called.  Also
         * this is not called by Fieldset Type.
         */
        this.afterUpdate = function(){};

        /**
         * Make a copy of the Types object.  It really returns a "default" value of a the current Type.  The "value"
         * will not be copied.
         * @instance
         * @method clone
         * @memberof TypeBase
         */
        this.clone = function(){
            var clone = jQuery.extend(true, {}, this);
            clone.value = null;
            return clone;
        };
    };

    //create the base prototype, some Types will override these
    Types.Text.prototype = new TypeBase();
    Types.Checkbox.prototype = new TypeBase();
    Types.Radio.prototype = new TypeBase();
    Types.Select.prototype = new TypeBase();
    Types.SelectMulti.prototype = new TypeBase();
    Types.TextArea.prototype = new TypeBase();
    Types.Button.prototype = new TypeBase();
    Types.DataList.prototype = new TypeBase();
    Types.KeyGen.prototype = new TypeBase();
    Types.Fieldset.prototype = new TypeBase();


    //specific for checkbox and radios
    Types.Checkbox.prototype.equals = function(type){return (type && this.type === type.type && this.value === type.value && this.name === type.name)};
    Types.Radio.prototype.equals = function(type){return (type && this.type === type.type && this.value === type.value && this.name === type.name)};

    Types.Checkbox.prototype.isChecked = function(){return this.checked};
    Types.Radio.prototype.isChecked = function(){return this.checked};

    Types.Checkbox.prototype.updateValue = function(json){
        if(!this.beforeUpdate()){
            if(console) console.log("Skipping update for "+this.getName());
            return;
        }
        this.$el.prop('checked',json.checked);this.$el.trigger("update");
        this.afterUpdate();
    };
    Types.Radio.prototype.updateValue = function(json){
        if(!this.beforeUpdate()){
            if(console) console.log("Skipping update for "+this.getName());
            return;
        }
        this.$el.prop('checked',json.checked);this.$el.trigger("update");
        this.afterUpdate();
    };

    Types.Checkbox.prototype.clone = function(){
        var clone = jQuery.extend(true, {}, this);
        clone.checked = false;
        return clone;
    };
    Types.Radio.prototype.clone = function(){
        var clone = jQuery.extend(true, {}, this);
        clone.checked = false;
        return clone;
    };

    //specific for fieldset
    Types.Fieldset.prototype.updateValue = function(elements){
        this.elements=elements;
    };

    Types.Fieldset.prototype.toJSON = function(){
        var result = {};
        for(var x in this.elements){
            if($.isArray(this.elements[x])){
                for(var y in this.elements[x]){
                    _add(result, this.elements[x][y], this.elements[x][y].toJSON());
                }
            }else{
                _add(result, this.elements[x], this.elements[x].toJSON());
            }
        }
        return result;
    };

    /**
     *  Convert a '<input type="x"' into a Type object.  Will default to Type.Text
     *  if not matched to any known types (checkbox, radio).
     */
    var inputToType = function($el){
        var name = $el.attr('name');
        var val = $el.val();
        var type = $el.attr('type') || 'text';

        switch(type.toLowerCase()){
            case 'button':
                return new Types.Button($el,name,val);
            case 'checkbox':
                return new Types.Checkbox($el,name,val,$el.is(':checked'));
            case 'radio':
                return new Types.Radio($el,name,val,$el.is(':checked'));
            default:
            case 'password':
            case 'text':
                return new Types.Text($el,name,val);
        }
    };

    /**
     * Create a Type object form an HTML Dom element.
     */
    var elToType = function($el){
        var name = $el.attr('name');
        var val = $el.val();
        var tag = $el.prop('tagName').toLowerCase();
        switch(tag){
            case 'input':
                return inputToType($el);
            case '':
                return new Types.Fieldset($el,name);
            case 'select':
                return new Types.Select($el,name,val);
            case 'selectMulti':
                return new Types.SelectMulti($el,name,val);
            case 'textarea':
                return new Types.TextArea($el,name,val);
            case 'button':
                return new Types.Button($el,name,val);
            case 'datalist':
                return new Types.DataList($el,name,val);
            case 'keygen':
                return new Types.KeyGen($el,name,val);
            case 'output':
                return new Types.Output($el,name,$el.text());
            case 'fieldset':
                return new Types.Fieldset($el, name, val);
            default:
                if(console) console.error('Unsupported type: '+tag+', will be skipped');
        }
    };

    /**
     * Apply the filters to the given Type object
     * @param type A Type object for current element.
     * @param obj This is a Types JSON object representation
     * @param filters The filters to apply
     */
    var _applyFilters = function(type, obj, filters){
        for(var filter in filters){
            if(typeof filters[filter] === 'function'){
                obj = filters[filter](type, obj);
            }
        }
        return obj;
    }

    /**
     * Iterate the form elements creating a schema, which is an Object comprised of Type objects.
     */
    var _schema = function(formEls, ctx){
        for(var i=0; i < formEls.length; i++){
            var $el = $(formEls[i]);

            if($el.attr('data-forms-visited') === 'true' || typeof $el.attr('name') === 'undefined') continue;

            var type = elToType($el);
            var name = type.getName();

            $el.attr('data-forms-visited',true);

            //special case <fieldset>
            if(type instanceof Types.Fieldset){
                var elements = {};
                _schema(formEls[i].elements, elements);
                type.updateValue(elements);

                if(typeof ctx[name] === 'undefined'){
                    ctx[name] = [];
                }
                ctx[name].push(type);

            //normal case, store object on result with the given name
            } else if(typeof ctx[name] === 'undefined'){
                ctx[name] = type;

            //we already have duplicate names, just add to the array
            }else if($.isArray(ctx[name])){
                ctx[name].push(type);

            //have an object, need to convert to array of objects.  Means duplicate name in the form
            }else{
                var temp = ctx[name];
                ctx[name] = [];
                ctx[name].push(temp);
                ctx[name].push(type);
            }
        }
    }


    /**
     * Make all elments as not visitied.
     */
    var _itrUnvisit = function(formEls){
        for(var i=0; i < formEls.length; i++){
            var $el = $(formEls[i]);
            $el.attr('data-forms-visited',false);
        }
    };

    /**
     * Wrapper for methods for working on a form element.
     * @class Forms
     * @constructor Forms
     * @param $form {jQuery} The jquery "&lt;form&gt;" element.
     * @param options {Object} Options configuration for form processing.
     */
    var Forms = function($form, options){
        var self = this;

        this.form = $form;

        var settings = {
            filterBase : true,
            filterSlim : true,
            filters : {
                extract:[],
                fill:[]
            }
        };

        if(options){
            $.extend(settings, options);
        }


        //add the slim filter.  This gives a minimal form representation.
        if(settings.filterSlim){
            //extract filter for slim
            settings.filters.extract.unshift(slimFilter.extract);

            //the slim fill filter.  Use the Type and json to rebuild the representation.
            settings.filters.fill.unshift(slimFilter.fill);
        };


        //this is the minimum filter for converting Type object to a serializable JSON representation.
        if(settings.filterBase || settings.filterSlim){
            //the most basic extract function
            settings.filters.extract.unshift(function(type, obj){return type.toJSON();});
        }

        /**
         * Fill a form from the JSON output and schema,  should really do an extract and iterate that along with the json.
         * This function needs to be rewritten.  Very brittle!
         */
        var _fill = function(json, schema, filters){

            for(var x in schema){

                var target;

                 if(json === null || typeof json === 'undefined' || typeof json[x] === 'undefined'){
                    target = null;
                 }else{
                    target = json[x];
                 }

                if(schema[x].type === 'Fieldset'){
                    _fill(target, schema[x].elements, filters);

                }else if($.isArray(schema[x])){
                    //possible this is a Fieldset Array, just the way the schemas work :(
                    if(schema[x][0] && schema[x][0].type === 'Fieldset'){
                        for(var y in schema[x]){
                            _fill(target, schema[x][y].elements, filters);
                        }

                    //maybe a checkbox or multiselect, but only 1 element given
                    }else if(!$.isArray(target)){
                        for(var y in schema[x]){
                            var temp = _applyFilters(schema[x][y], target, filters);
                            schema[x][y].updateValue(temp);
                        }
                    }else{

                        //possible we have fewer entries in JSON than we have in
                        //the schema, may need to skip schema entries
                        var skips = 0;

                        for(var y in schema[x]){
                            //have to apply filters, so we can compare element with schema
                            var temp = _applyFilters(schema[x][y], target[y-skips], filters);

                            //possible we are missing elements in json, that schema has.  So skip.
                            if(!schema[x][y].equals(temp) && schema[x][y].type !== 'Fieldset'){
                                schema[x][y].updateValue(schema[x][y].clone());
                                skips++;
                                continue;
                            }

                            var sname = schema[x][y].name;
                            var temp1 = {};
                            var temp2 = {};
                            temp1[sname] = schema[x][y];
                            temp2[sname] = target[y-skips];
                            _fill(temp2, temp1, filters);
                        }
                    }
                }else{
                    schema[x].updateValue(_applyFilters(schema[x], target, filters));
                }
            }
        };

        var _extract = function(schema, result, filters){
            for(var x in schema){
                if(schema[x].type === 'Fieldset'){
                    var result2 = {};
                    _extract(schema[x].elements, result2, filters);
                    _add(result,schema[x],result2);


                }else if($.isArray(schema[x])){
                    for(var y in schema[x]){
                        _extract([schema[x][y]], result, filters);
                    }
                }else{
                    _add(result,schema[x],_applyFilters(schema[x], schema[x], filters));
                }
            }
            return result;
        };

        /**
         * Let user's create {@link Types}.
         * @instance
         * @memberof Forms
         */
        this.Types = Types;

        /**
         * All the filters that will be applied.  Depending on the options passed in at construction, you will
         * be given some default filters.
         * @memberof Forms
         * @instance
         */
        this.filters = settings.filters;

        /**
         * Add a filter which happens at fill time.  You will be given a properly filled
         * Type object to work with.  Meaning, this adds to the end of the fill process.
         * @param fn {function} The function to be called during the filter process.  The signature is
         * fn(Type, JSON).  Where the Type is a Types object that represents the form elment.
         * And JSON is the incoming representation of the form element.  You should modify the the JSON and
         * return it.  The returned element should be in the same format as Types.Type.toJSON() output.
         * @memberof Forms
         * @method addFillFilter
         * @instance
         */
        this.addFillFilter = function(fn){this.filters.fill.push(fn);};

        /**
         * Add a filter that happens at the extract time.  This adds to filter at index 1.  Meaning it is the second
         * filter called.  The first filter is always calling Types.Type.toJSON() so you will be working on the
         * JSON representation of the form element.
         *
         * @param fn {function} The function to be called during the filter process.  The signature is
         * fn(Type, JSON).  Where the Type is a Types object that represents the form elment.
         * And JSON is the incoming representation of the form element.  You should modify the the JSON and
         * return it.  The returned element should be in the same format as Types.Type.toJSON() output.
         * @memberof Forms
         * @method addExtractFilter
         * @instance
         */
        this.addExtractFilter = function(fn){this.filters.extract.splice(1,0,fn);};

        /**
         * Add a filter that will only filter on the form element's "name" attribute.
         * @memberof Forms
         * @param name {String|RegExp}  Will check the elements name.
         * @param fn {function} The function to apply if the name matches.  See {@link #addFillFilter}.
         * @method addNameFillFilter
         * @instance
         */
        this.addNameFillFilter = function(name, fn){
            this.addFillFilter(this._filter(name, function(type){return type.name}, fn));
        };

        /**
         * Add a filter that will only filter on the form element's "name" attribute.
         * @memberof Forms
         * @param name {String|RegExp}  Will check the elements name.
         * @param fn {function} The function to apply if the name matches.  See {@link #addFillFilter}.
         * @instance
         * @method addNameExtractFilter
         */
        this.addNameExtractFilter = function(name, fn){
            this.addExtractFilter(this._filter(name,function(type){return type.name},fn));
        };

        /**
         * Add a filter that will only filter on the form element's tag name.
         * @memberof Forms
         * @param name {String|RegExp}  Will check the elements tag name.
         * @param fn {function} The function to apply if the name matches.  See {@link #addFillFilter}.
         * @instance
         * @method addTypeFillFilter
         */
        this.addTypeFillFilter = function(typeName, fn){
            this.addFillFilter(this._filter(typeName, function(type){return type.$el.tagName}, fn));
        };

        /**
         * Add a filter that will only filter on the form element's tag name.
         * @memberof Forms
         * @param name {String|RegExp}  Will check the elements tag name.
         * @param fn {function} The function to apply if the name matches.  See {@link #addFillFilter}.
         * @instance
         * @method addTypeExtractFilter
         */
        this.addTypeExtractFilter = function(typeName, fn){
            this.addExtractFilter(this._filter(typeName, function(type){return type.$el.tagName}, fn));
        };

        /**
         * Used by filter functions to avoid code duplication.
         * @memberof Forms
         * @param name {String} The String or RegExp to be used for comparison.
         * @param get {function} A function that returns the value from a Types.Type to be compared to {@param name}
         * @param fn {function} A function to apply if a comparison is true.
         * @instance
         */
        this._filter = function(name, get, fn){
            return function(type, json){
                   if(name instanceof RegExp){
                       if(name.test(get(type))){
                           return fn(type,json);
                       }
                   }else if(name === get(type)){
                       return fn(type,json);
                   }
                   return json;
               };
        };

        /**
         * Fill the form given the json.  This will perform filters on the given json.
         * @memberof Forms
         * @param json {Object} A JSON object created from an extract call.
         * @instance
         * @method fill
         */
        this.fill = function(json){
            _fill(json, this.getSchema(), this.filters.fill);
        };

        /**
         * Get the this#filters object.
         * @memberof Forms
         * @instance
         * @method getFilters
         * @return the filters used by this Form object.
         */
        this.getFilters = function(){
            return this.filters;
        };

        /**
         * Extract the current form element data into a JSON object which can be serialized.  The filters will be applied
         * during the extract process.
         * @instance
         * @method extract
         * @memberof Forms
         * @return {Object} The json representation of form and values after going through filters.
         */
        this.extract = function(){
            var result = {};
            var schema = this.getSchema();

            _extract(schema, result, this.filters.extract);
            return result;
        };

        /**
         * Get a JSON object comprised of name and Types.Type that represent this form.
         * @memberof Forms
         * @instance
         * @method getSchema
         * @return {Object} A json object representing the form.
         */
        this.getSchema = function(){
            var output = {};

            var formName = $form.attr('name');

            if(!formName) return output;

            _schema(document.forms[formName].elements, output);
            _itrUnvisit(document.forms[formName].elements); //need to unmark elements as visited

            return output;
        };

        /**
         * Get a reference to TypeBase which is the base class for all Types.  This allows for prototyping.  All Types
         * extend this Object.  However, some Types implement their own methods.  So not all Type objects are guranteed
         * to use these implementations.  Example would be Types.Checkbox.updateValue().  This has a prototype method for updateValue().
         * So you would need to use {@link #getType("Checkbox")} to override its behavior.
         * @memberof Forms
         * @instance
         * @method getTypeBase
         * @return {TypeBase}
         */
        this.getTypeBase = function(){
            return TypeBase;
        };

        /**
         * Get a reference to the Types[TypeName].  This allows for prototyping.  These objects actually extend
         * TypeBase.  So if you want to override a method for all Types use the {@link #getTypeBase}.  Use this
         * if you want to override behavior for specific Type.
         * Example:
         * <pre>$('form').forms(function(form){
         *     form.getType("Text").prototype.beforeUpdate = function(){
         *         console.log(this.value);
         *     }
         * });</pre>
         * @memberof Forms
         * @instance
         * @method getType
         * @return {Types}
         */
        this.getType = function(TypeName){
            return Types[TypeName];
        };
    };


    /**
     * The forms namespace extends jQuery.fn to be a jQuery plugin.
     * @namespace "$.fn.forms"
     * @function
     * @param options {Object|function} If Object, then the options are applied to {@link Forms}.  If function, then the
     * function is called being passed the {@link Forms} object as a first parameter to the function.
     * @return {jQuery} The matched jQuery element.
     */
    $.fn.forms = function(options){

        //all the elements from jquery selector, expecting "form" tags
        this.each(function(index,el){
            var $el = $(el);
            var form = $el.data("forms");
            if(!form){
                form = new Forms($el, options);
                $el.data("forms", form);
            }

            if(typeof options === 'function'){
                options(form);
            }
        });

        return this;
    }


})(jQuery);