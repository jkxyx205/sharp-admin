(function($) {
    /**
     * 兄弟组件ajax-table & table-column，通信问题，定义一个空的Vue实例做消息
     */
    Vue.component('ajax-table', {
        data: function () {
            return {
                props:[],
                containerId: '',
                query: {
                    page: 1,
                    size: 15,
                }
            }
        },
        props: {
            rowKey: {
                type: String
            },
            grid: {
                type: Object,
                default:function () {
                    return {
                        page: 1,
                        rows: []
                    }
                }
            },
            showSummary: {
                type: Boolean,
                default: false
            },
            ignoreSummaryIndex: {
                type: Array,
                default: function() {
                    return []
                }
            },
            summaryText: {
                type: String,
                default: '合计'
            },
            layoutFixed: { // 配合colResizable，同时设置固定宽度
                type: Boolean,
                default: false
            },
            fixedHead: { // 设置true，头部固定，同时设置固定宽度。
                type: Boolean,
                default: false
            },
            displayPage: {
                type: Number,
                default: 5
            }
        },
        computed: {
            pageInfo: function () {
                return limitPages(this.grid.totalPages, this.displayPage, this.grid.page)
            }
        },
        directives: {
            text2: {
                inserted: function (el, binding, vnode) {
                    if (!vnode.key) { // 注释这个 点击"累计预留数量"排序，序号6变12 ？？
                        vnode.key = Math.floor(Math.random()*1000000)
                    }

                    binding.value.show && binding.value.text && (el.title = binding.value.text)
                    el.innerHTML =  !binding.value.text ? '' : binding.value.text
                    el.setAttribute("data-sort-value", binding.value.original === null ? '' : binding.value.original)
                }
            }
        },
        watch: {
            grid: function (new_value) {
                this.query.page = new_value.page
                this.$container.scrollTop(0)
            }
        },
        updated: function() {
            this.$container.table('refresh')
            this.$emit('refresh')
            if (this.grid.sidx) {
                this.$container.table('initSortByParams', {
                    sidx: this.grid.sidx,
                    sord: this.grid.sord
                })
            }
        },
        created: function() {
            this.containerId = "table-container-" + new Date().getTime()
        },
        mounted: function() {
            this.$event = new Vue()
            var _this = this
            this.$event.$on('props',function (prop) {
                _this.props.push(prop)
            })

            this.$nextTick(function () {
                var _this = this
                this.$container = $('#' + this.containerId)
                this.$container.table({
                    type: 'server',
                    fixedHead: _this.fixedHead,
                    summary: _this.showSummary,
                    ignoreSummaryIndex: _this.ignoreSummaryIndex,
                    summaryText: _this.summaryText,
                    clicked: function (o, e) {
                        _this.$emit('sort-column', $(e).data('name'), e.sord)
                    }
                })
                // 集成有问题
                // setTimeout(function () {
                //     _this.$container.find('table').colResizable({resizeMode:'overflow', useLocalStorage: false, postbackSafe: false})
                // }, 100)
            })
        },
        // language=HTML
        template: '<div :id="containerId">\n<div :class="(fixedHead || layoutFixed) ? \'table-fixed-container\' : \'card-body-scroll-panel\'">\n    <table class="table table-responsive-sm table-bordered table-striped table-sm table-thead" v-if="fixedHead">\n        <thead>\n            <slot></slot>\n        </thead>\n    </table>\n    <table class="table table-responsive-sm table-bordered table-striped table-sm table-tbody">\n        <thead v-if="!fixedHead">\n            <slot></slot>\n        </thead>\n        <tbody>\n        <tr @click="$emit(\'row-click\', row, $event)" @dblclick="$emit(\'row-dbclick\', row, $event)" v-for="(row, index) in grid.rows" :key="row[rowKey] ? row[rowKey] : Math.floor(Math.random()*1000000)">\n            <td :class="\'text-\'+p.align" v-text2="{ show: p.tooltip, original: row[p.prop] ,text: typeof p.$scopedSlots.default === \'function\' ? p.$scopedSlots.default({\'row\': row})[0].text : (p.type === \'index\' ? (index + 1 + (grid.page - 1) *grid.pageSize) : row[p.prop])}" v-for="p in props"></td>\n        </tr>\n        <tr class="tr-empty non-data" v-if="grid.rows && grid.rows < 1">\n            <td :colspan="props.length" style="text-align: center;"><span class="empty-text">暂无数据</span></td>\n        </tr>\n        </tbody>\n    </table>\n</div>\n<div class="table-footer-bar clearfix">\n    <div class="pull-left" style="margin-bottom: 8px;">\n        <span class="breadcrumb-item active">共{{ grid.records }}条，{{ grid.totalPages }}页</span>\n        <select @change="$emit(\'size-change\', query.size)" v-model="query.size">\n            <option value="15" selected="selected">15条</option>\n            <option value="50">50条</option>\n            <option value="100">100条</option>\n            <option value="200">200条</option>\n        </select>\n        <span v-show="grid.totalPages > 1">，前往<input class="goto" @keyup.enter="$emit(\'page-change\', query.page)" maxlength="6" v-model="query.page">页</span>\n    </div>\n    <nav class="pull-right" v-show="grid.totalPages > 1">\n        <ul class="pagination pagination-sm">\n            <li class="page-item" v-if="grid.page > 1">\n                <a class="page-link" href="javascript:;" @click="$emit(\'page-change\', query.page - 1)">上一页</a>\n            </li>\n            <li class="page-item" :class="{\'active\': i === grid.page}" v-for="i in pageInfo.endPage" v-if="pageInfo.startPage <= i">\n                <a class="page-link" href="javascript:;" @click="$emit(\'page-change\', i)">{{ i }}</a>\n            </li>\n            <li class="page-item" v-if="grid.page < grid.totalPages">\n                <a class="page-link" href="javascript:;" @click="$emit(\'page-change\', query.page + 1)">下一页</a>\n            </li>\n        </ul>\n    </nav>\n</div>\n</div>'
    })

    Vue.component('table-column', {
        data: function () {
            return {
            }
        },
        props: {
            prop: {
                type: String
            },
            label: {
                type: String
            },
            width: {
                type: Number
            },
            type: {
                type: String
            },
            align: {
                type: String,
                default: 'left'
            },
            sortable: {
                type: Boolean,
                default: false
            },
            tooltip: {
                type: Boolean,
                default: false
            }
        },
        mounted: function() {
            var _this = this
            this.$nextTick(function () {
                _this.$parent.$event.$emit('props', {
                    'prop': _this.prop,
                    'label': _this.label,
                    'width': _this.width,
                    'type': _this.type,
                    'align': _this.align,
                    'tooltip': _this.tooltip,
                    '$scopedSlots':_this.$scopedSlots,
                })
            })
        },
        // language=HTML
        template: '<th :data-name="prop" :style="\'width:\'+width+\'px\'" :class="[ {sortable: sortable}, \'text-\' + align]">{{ label }}</th>'
    })

    var AjaxTable = function(element, options) {
        this.element = element
        // this.$element
        this.options = $.extend({},
            $.fn.ajaxTable.defaults, options); //合并参数

        this.init();
    };

    AjaxTable.prototype = {
        constructor: AjaxTable,
        init: function() {
            var _this = this

            this.$vue = new Vue({
                el: _this.element,
                data: {
                    grid: {},
                    query: _this.options.query
                },
                computed: {
                    displayPage: function () {
                        return _this.options.displayPage()
                    }
                },
                mounted: function () {
                    this.search()
                    this.$nextTick(function () {
                        _this.options.mounted && _this.options.mounted()
                        _this.$element = $('#' + _this.element.id);
                        this.$saleListTbody = _this.$element.find('table tbody')
                        this.saleListColumnSize = _this.$element.find('thead th').length
                    })
                },
                methods: {
                    search: function (params) {
                        if (!params || typeof params !== 'object') {
                            params = {}
                        }
                        this.query = $.extend({}, this.query, params)

                        var that = this
                        // trim
                        this.queryTrim()
                        $.get(_this.options.url, this.query, function (res) {
                            if (res.success) {
                                var grid = res.data;
                                grid.sidx = that.query.sidx
                                grid.sord = that.query.sord

                                that.grid = grid
                                that.query.page = that.grid.page
                            } else {
                                toastr.error(res.msg)
                            }
                        })
                    },
                    pageChange: function(currentPage) {
                        if (currentPage === this.query.page)
                            return

                        this.search({page: currentPage})
                    },
                    sortColumn: function (sidx, sord) {
                        this.query.page = 1
                        this.query.sidx = sidx
                        this.query.sord = sord
                        this.search()
                    },
                    sizeChange: function (size) {
                        this.query.page = 1
                        this.query.size = size
                        this.search()
                    },
                    rowClick: function (row, event) {
                        window[_this.element.id + 'RowClick'] && window[_this.element.id + 'RowClick'](row, event)
                    },
                    rowDbclick: function(row, event) {
                        window[_this.element.id + 'RowDbclick'] && window[_this.element.id + 'RowDbclick'](row, event)
                    },
                    queryTrim: function () {
                        var trimQuery = {}
                        for(var p in this.query) {
                            var value = this.query[p]
                            if (typeof value === 'string')
                                value = value.trim()

                            trimQuery[p] = value
                        }
                        this.query = trimQuery
                    },
                    refresh: function () {
                        this._handleSummary()
                    },
                    _handleSummary: function() {
                        if (!_this.options.count) {
                            return
                        }

                        if (!(this.grid.records && this.grid.records > 0))
                            return

                        var that = this

                        var isEquals = _.isEqual(this.query, this.oldParams)

                        if (!isEquals) {
                            console.log('.....fetch summary data')
                            that.data = [] // ajax
                            $.ajax({
                                method: 'GET',
                                url: _this.options.count,
                                data: this.query,
                                success: function (res) {
                                    if (res.success) {
                                        that.data =  res.data
                                        appendSummary()
                                    } else {
                                        toastr.error(res.msg)
                                    }
                                }
                            })
                        } else {
                            appendSummary()
                        }

                        function appendSummary() {
                            that.oldParams = that.query

                            var summaryIndex = _this.options.summaryIndex
                            var summaryData = new Array(that.saleListColumnSize)
                            summaryData[0] = '合计'
                            var summarySize = summaryIndex.length
                            for (var i = 0 ; i < summarySize; i++) {
                                var summaryVal = summaryIndex[i]

                                if (typeof summaryVal === 'object') {
                                    summaryData[summaryVal.index] = summaryVal.name
                                } else {
                                    summaryData[summaryVal] = formatInteger(that.data[i])
                                }
                            }
                            that.$saleListTbody.append('<tr class="tr-summary non-data" style="border-top: 0;"><td class="text-right bold">' + summaryData.join('</span></td><td class="text-right bold"><span>') + '</td></tr>')
                        }
                    }
                }
            })
        },
        reload: function(params) {
            this.$vue.search(params)
        },
        getQuery: function() {
            return this.$vue.$data.query
        }
    }

    $.fn.ajaxTable = function(options) {
        options = options || {}
        var args = arguments;
        var value;
        var chain = this.each(function() {
            data = $(this).data("ajaxTable");
            if (!data) {
                if (options && typeof options == 'object') { //初始化
                    return $(this).data("ajaxTable", data = new AjaxTable(this, options));
                }
            } else {
                if (typeof options == 'string') {
                    if (data[options] instanceof Function) { //调用方法
                        var property = options; [].shift.apply(args);
                        value = data[property].apply(data, args);
                    } else { //获取属性
                        return value = data.options[options];
                    }
                }
            }

        });

        if (value !== undefined) {
            return value;
        } else {
            return chain;
        }

    };

    $.fn.ajaxTable.defaults = {

    };
})(jQuery);
