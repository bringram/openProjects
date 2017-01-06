'use strict';

var RafNav = function(){

    /**
     * Utility to create a DOM element
     */
    var _el = function(el) {
        var obj = document.createElement(el);

        for(var x=1; x<arguments.length;x=x+2){
            var name = arguments[x];
            var val = arguments[x+1];
            (val === "class") ? obj.addClass(val) : obj._attr_(name,val);
        }

        return obj;
    };

    /**
     * Utility for adding class names
     */
    var addClass = function(el, className) {
        className.split(" ").forEach(function(name) {
            el.classList.add(name)
        });
    }

    var RafNode = function(txt, type, parent){
        this.el = _el((type?type:'li'));
        this._rendered = false;
        this._before = '';
        this._after = '';
        this._child = [];
        this._href = null;

        this.up = function(){
            return parent;
        };
        this.parent = this.up;
        this.attr = function(name, value){
            this.el.setAttribute(name, value);
            return this;
        };
        this.id = function(id){
            this.attr('id',id);
            return this;
        };
        this.href = function(link){
            this._href = link;
            return this;
        };
        this.clazz = function(name){
            addClass(this.el, name);
            return this;
        };
        this.before = function(txt){
            this._before = txt;
            return this;
        };
        this.after = function(txt){
            this.after = txt;
            return this;
        };
        this.add = function(txt, tagName){
            if(typeof tagName === 'undefined') tagName = 'li';
            var node = new RafNode(txt, tagName, this);
            this._child.push(node);
            return node;
        };
        this.append = function(rafNode){
            this._child.push(rafNode);
            return this;
        };
        this.root = function(){
            var el, p = this;
            while(p){
                el = p.el;
                p = p.parent();
            }
            return el;
        };

        this.render = function(){
            if(this._rendered){
                return this.root();
            }

            this._rendered = true;

            var _inner = this._before + txt + this._after;
            if(this._href){
                this.el.appendChild(new RafNode(_inner, 'a').attr('href',this._href).render());
            }else{
                this.el.innerHTML = _inner;
            }

            if(this._child.length){
                var target = (this.el.tagName.toUpperCase() === 'LI') ? _el('ul') : this.el;
                var ul = _el('ul');
                this._child.forEach(function(node){
                    target.appendChild(node.el);
                    node.render();
                });

                if(target !== this.el){
                    this.el.appendChild(target);
                }
            }

            if(parent) parent.render();

            return this.root();
        };
    };

    this.Left = function(){
        return new RafNode('','div').add('','ul');
    };

    this.Top = function(){
        return new RafNode('','div').add('','ul');
    };

    this.TopTitle = function(txt){
        return new RafNode('','div').clazz('raf-title dhl-bg-yellow-gd')
            .add(txt,'span').clazz('raf-lg dhl-f-red');
    };
};