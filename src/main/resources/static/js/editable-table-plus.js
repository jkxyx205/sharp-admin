;(function($) {
    let EditableTablePlus = function(element, options) {
        this.$element = $(element);
        this.options = $.extend({},
            $.fn.editableTablePlus.defaults, options); //合并参数
        this.init();
    };

    EditableTablePlus.prototype = {
        constructor: EditableTablePlus,
        init: function() {
            let columnTitles = [];
            if (this.options.showRowNumber) {
                columnTitles.push('<th style=\"width: 50px\">序号</th>')
            }

            for(let columnConfig of this.options.columnConfigs) {
                if (columnConfig.type === 'hidden') {
                    continue
                }

                let width = columnConfig.width ? columnConfig.width + 'px' : 'auto'
                let align = columnConfig.align ? columnConfig.align  : 'left'

                columnTitles.push('<th style="width: ' + width + '; text-align: '+align+'">');
                columnTitles.push('<label'+(columnConfig.required ? " class=\"required\"" : "")+'>'+columnConfig.title+'</label>')
                columnTitles.push('</th>');
            }

            columnTitles.push('<th style="width: 50px;"></th>')

            let template = "<div class=\"table-fixed-container\">\n" +
                "                            <table class=\"table-thead table-editor table table-responsive-sm table-striped table-sm\">\n" +
                "                                <thead>\n" +
                "                                <tr>"+columnTitles.join('')+"</tr>\n" +
                "                                </thead>\n" +
                "                            </table>\n" +
                "\n" +
                "                            <table class=\"table-tbody table-editor table table-responsive-sm table-bordered table-sm\">\n" +
                "                                <tbody>\n" +
                "                                </tbody>\n" +
                "                            </table>\n" +
                "</div>"

            this.$element.html(template)
            this.$table = this.$element.find('table.table-tbody')

            let _this = this

            this.$table.editableTable({
                columns: this.options.columnConfigs.filter(c => c.type !== 'hidden').length + (this.options.showRowNumber ? 1 : 0),
                addEmptyLineCallback: function ($tr) {
                    _this._formatTr($tr)
                    _this._rebuildIndex()
                    _this._setRequired();
                },
                beforeRemoveCallback: function ($parent) {
                    if ( _this.options.beforeDeleteRow) {
                        let deleted =  _this.options.beforeDeleteRow(_this, $parent, _this._getValue($parent))
                        if (deleted) {
                            _this.options.activeIndex--
                        }
                        return deleted
                    }

                    return true
                },
                afterRemoveCallback: function ($parent) {
                    _this._rebuildIndex()
                    return true
                }
            })

            // 初始化值
            let hasData = this.options.value != undefined && this.options.value.length > 0
            if (hasData) {
                this.appendValue(this.options.value)
            }

            // 固定表头
            this.$element.table({fixedHead: true})
            if (!this.options.readonly || hasData) {
                $('.tr-empty.non-data').remove()
            }

            this.readonly(this.options.readonly)

            // 注册事件
            if (this.options.rowClick || this.options.highlight) {
                this.$table.delegate('tr', 'click', (e) => {
                    let $tr = $(e.target).parents('tr')
                    this.options.activeIndex = $tr.index() + 1 // 第一行从 1 开始

                    if (this.focusRow !== $tr[0]) {
                        // 多次点击 保证只触发一次请求
                        this.options.rowClick(this, $tr, this._getValue($tr))
                        this.focusRow = $tr[0]

                        this.setActiveIndex(this.options.activeIndex)
                    }
                })
            }

            for (let i = 0; i < this.options.columnConfigs.length; i++) {
                let columnConfig = this.options.columnConfigs[i];
                this.options.customizeType[columnConfig.type] &&
                this.options.customizeType[columnConfig.type].mounted &&
                this.options.customizeType[columnConfig.type].mounted(columnConfig)
            }
        },
        getEditRow: function () {
            return this.$table.find('input[name=id][value]').parent()
        },
        getValue: function() {
            let valueList = []
            this.$table.find('tbody tr:not(:last-child)').each((index, elem) => {
                valueList.push(this._getValue($(elem)))
            })

            return valueList
        },
        setValue: function (value) {
            if (value && value.length > 0) {
                this.clear()
                this.appendValue(value)
            } else {
                // 清空表格
                this.clear()
            }
        },
        setActiveIndex: function (index) {
            this.options.activeIndex = index
            // highlight
            if (this.options.highlight) {
                this.$table.find('tr:nth-child('+this.options.activeIndex+')')
                    .css('border-left', '4px solid rgb(32, 168, 216)')
                    .siblings().css('border-left', 'none')
            }
        },
        each: function (callback) {
            this.$table.find('tbody tr:not(:last-child)').each((index, elem) => {
                callback(index, this, $(elem), this._getValue($(elem)))
            })
        },
        readonly:function (readonly) {
            this.options.readonly = readonly
            this.$table.editableTable('readonly', this.options.readonly)
            if (this.options.readonly) {
                this.$element.find('thead th:last-child').hide()
            } else {
                this.$element.find('thead th:last-child').show()
                // readonly 有 true => false, 根据 _formatTr 重新设置 readonly 和 disabled
                let _this = this
                this.$table.find('tbody tr').each(function() {
                    _this._consumeUnHiddenColumnConfig((childIndex, columnConfig) => {
                        let $input = $(this).find('td:nth-child('+childIndex+') :input')

                        if (columnConfig.disabled === true) {
                            $input.prop('disabled', true).attr('readonly', true)
                        }
                    })
                })
            }
        },
        getColumnConfigs: function () {
            return this.options.columnConfigs
        },
        clear: function () {
            if (this.$table.find('tbody tr').length > 1) {
                this.$table.find('tbody tr:not(:last-child)').remove()
                this._rebuildIndex()
            }
        },
        appendValue: function (value) {
            for (let rowValue of value) {
                this.$table.editableTable('addEmptyLine')
                let $tr = this.$table.find("tr:last-child").prev()
                $tr.show()

                $tr.find(":input[name]").each(function () {
                    if ($(this).attr('type') === 'switch') {
                        $(this).prop('checked', eval(rowValue[this.name]))
                    } if ($(this).attr('type') === 'checkbox') {
                        if ($(this).data('type') === 'multi_checkbox') {
                            if (rowValue[this.name] && rowValue[this.name].length > 0) {
                                for (let id of rowValue[this.name]) {
                                    $("input[name="+this.name+"][value=" + id + "]").prop('checked', true);
                                }
                            }
                        } else {
                            $(this).prop('checked', eval(rowValue[this.name]))
                        }
                    } else if ($(this).attr('type') === 'radio') {
                        $("input[name="+this.name+"][value=" + rowValue[this.name.substring(0, this.name.lastIndexOf('-'))] + "]").prop('checked', true);
                    } else {
                        $(this).val(rowValue[this.name])
                    }
                })
            }
        },
        _getValue: function ($tr) {
            let value = {}
            $tr.find(':input').each(function () {
                let name = $(this).attr('name')
                if (name) {
                    if ($(this).attr('type') === 'switch') {
                        value[name] = $(this).prop('checked')
                    } else if ($(this).attr('type') === 'checkbox') {
                        if ($(this).data('type') === 'multi_checkbox') {
                            if (value[name] == undefined) {
                                value[name] = []
                            }
                            if ($(this).is(":checked")) {
                                value[name].push($(this).val())
                            }
                        } else {
                            value[name] = $(this).prop('checked')
                        }
                    } else if ($(this).attr('type') === 'radio') {
                        value[name.substring(0, name.lastIndexOf('-'))] = $('input[name="'+name+'"]:checked').val()
                    } else {
                        value[name] = $(this).val()
                    }
                }
            })

            return value
        },
        _formatTr: function($tr) {
            let hiddenColumnConfigs = this._consumeUnHiddenColumnConfig((childIndex, columnConfig) => {
                let $td = $tr.find('td:nth-child('+childIndex+')')
                let $input = $tr.find('td:nth-child('+childIndex+') input')

                $input.attr('name', columnConfig.name)
                    .css('text-align', columnConfig.align ?  columnConfig.align : 'left')

                if (columnConfig.disabled) {
                    $input.attr('disabled', true).attr('readonly', true)
                }

                if (columnConfig.type === 'text') {

                } else if (columnConfig.type === 'number') {
                    $input
                        .attr('type', 'number')
                        .attr('title', '必须整数')
                        .on('keyup', function (event) {
                            columnConfig.keyup && columnConfig.keyup($tr, event, $(this).val())
                        })
                } else if(columnConfig.type === 'label') {
                    // 等于 text === 'text' && disabled = true
                    columnConfig.disabled = true
                    $input.attr('disabled', true).attr('readonly', true)
                } else if(columnConfig.type === 'text_label') {
                    $input.attr('disabled', true).attr('readonly', true).attr("name", columnConfig.name + "Text")
                    $tr.find('td:nth-child('+childIndex+')').append('<input class="form-control" type="hidden" name="'+columnConfig.name+'">')
                } else if(columnConfig.type === 'checkbox') {
                    if (columnConfig.datasource) {
                        columnConfig.type = 'multi_checkbox' // 多选
                        let checkboxs = []
                        for(let v of columnConfig.datasource) {
                            let id = v.name + $tr.index()
                            checkboxs.push('<input type="checkbox" data-type="multi_checkbox" name="'+columnConfig.name+'" value="'+v.name+'" id="'+id+'"><label for="'+id+'">'+v.value+'</label>')
                        }
                        let $checkboxs = $(checkboxs.join(''))

                        $td.html($checkboxs).css('text-align', columnConfig.align ?  columnConfig.align : 'left')

                        if (columnConfig.onchange) {
                            $checkboxs.siblings('input').on('change', function () {
                                columnConfig.onchange($tr, this.value)
                            })
                        }
                    } else {
                        // 单个checkbox
                        $input.attr('type', 'checkbox').attr('class', '').parent().css('text-align', columnConfig.align ?  columnConfig.align : 'left')
                    }
                } else if(columnConfig.type === 'switch') {
                    let $switch = $('<label style="position: relative; top: 3px;" class="switch switch-pill switch-primary">\n' +
                        '                      <input class="switch-input" type="checkbox" name="'+columnConfig.name+'">\n' +
                        '                      <span class="switch-slider"></span>\n' +
                        '                    </label>')
                    $td.html($switch).css('text-align', columnConfig.align ?  columnConfig.align : 'left')
                } else if (columnConfig.type === 'select') {
                    let optionHTML = ''
                    if (columnConfig.datasource) {
                        if (Array.isArray(columnConfig.datasource)) { // 数组
                            let options = []
                            for(let v of columnConfig.datasource) {
                                options.push('<option value="'+v.name+'">'+v.value+'</option>')
                            }
                            optionHTML = options.join('')
                        } else if ((columnConfig.datasource && (columnConfig.datasource instanceof jQuery || columnConfig.datasource.constructor.prototype.jquery))) {
                            // select jquery对象
                            optionHTML = columnConfig.datasource.html()
                        }
                    }

                    let $select = $('<select class="form-control"'+(columnConfig.disabled ? ' disabled' : '')+' name="'+columnConfig.name+'">'+optionHTML+'</select>')
                    $td.html($select);

                    if (columnConfig.onchange) {
                        $select.on('change', function (e) {
                            columnConfig.onchange($tr, this.value, e)
                        })
                    }
                } else if (columnConfig.type === 'radio') {
                    let radios = []
                    if (columnConfig.datasource) {
                        for(let v of columnConfig.datasource) {
                            let id = v.name + $tr.index()
                            radios.push('<input type="radio" name="'+(columnConfig.name + '-' + $tr.index())+'" value="'+v.name+'" id="'+id+'"><label for="'+id+'">'+v.value+'</label>')
                        }
                    }

                    let $radios = $(radios.join(''))

                    $td.html($radios);

                    if (columnConfig.onchange) {
                        $radios.siblings('input').on('change', function (e) {
                            columnConfig.onchange($tr, this.value, e)
                        })
                    }
                } else if (columnConfig.type === 'decimal') {
                    $input
                        .attr('pattern', '^\\d+(\\.\\d{1,3})?$')
                        .attr('title', '必须数字')

                    // 注册事件
                    $input.on('keydown', function(event){
                        if ((event.keyCode > 57 || event.keyCode < 48) && event.keyCode !== 8 && event.keyCode !== 190 && event.keyCode !== 39 && event.keyCode !== 37 && event.keyCode !== 9) {
                            event.preventDefault();
                            event.stopPropagation()
                            return false;
                        }
                    }).on('keyup', function (event) {
                        columnConfig.keyup && columnConfig.keyup($tr, event, $(this).val())
                    })
                } else if(columnConfig.type === 'date') {
                    $td.find('input').datepicker({
                        language: "zh-CN",
                        autoclose: true,
                        clearBtn: true,
                        todayBtn: 'linked',
                        todayHighlight: true,
                        format: 'yyyy-mm-dd'
                    })
                } else {
                    this.options.customizeType[columnConfig.type] && this.options.customizeType[columnConfig.type].formatTr($td, columnConfig)
                }
            })

            // 添加隐藏域
            hiddenColumnConfigs.forEach(columnConfig => $tr.append('<input type="hidden" name="'+columnConfig.name+'">'))
            // id
            $tr.append('<input type="hidden" name="id">')
        },
        _rebuildIndex: function () {
            // 设置行号
            if (this.options.showRowNumber) {
                let editableTableIndex = 1;
                this.$table.find('tbody tr').each(function () {
                    $(this).find('td:nth-child(1)').text(editableTableIndex++).addClass('row-number')
                })
            }
        },
        _setRequired: function () {
            // 倒数第二行 设置 required
            let $tr = this.$table.find('tbody tr:last-child')
            let $requiredTr = $tr.prev()
            if ($requiredTr.length == 0) {
                $requiredTr = $tr

                if (this.options.allowEmpty) {
                    return
                }
            }

            this._consumeUnHiddenColumnConfig((childIndex, columnConfig) => {
                if (columnConfig.required === true) {
                    let $input = $requiredTr.find('td:nth-child('+childIndex+') :input')
                    let title = $input.attr('title') ? $input.attr('title') : '请填写' + columnConfig.title
                    $input.attr('required', true)
                        .attr('title', title)
                }
            })
        },
        _consumeUnHiddenColumnConfig: function (consumer) {
            let hiddenColumnConfigs = []
            let tdIndex = 0
            for (let i = 0; i < this.options.columnConfigs.length; i++) {
                let columnConfig = this.options.columnConfigs[i];
                if (columnConfig.type === 'hidden') {
                    hiddenColumnConfigs.push(columnConfig)
                    continue
                }

                let childIndex = (tdIndex++) + 1 + (this.options.showRowNumber ? 1 : 0)
                consumer(childIndex, columnConfig)
            }

            return hiddenColumnConfigs
        }
    }

    $.fn.editableTablePlus = function(options) {
        options = options || {}
        var args = arguments;
        var value;
        var chain = this.each(function() {
            data = $(this).data("editableTablePlus");
            if (!data) {
                if (options && typeof options == 'object') { //初始化
                    return $(this).data("editableTablePlus", data = new EditableTablePlus(this, options));
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

    $.fn.editableTablePlus.defaults = {
        showRowNumber: true, // 是否显示行号
        readonly: false, // 只读
        allowEmpty: false, // 表格允许为空,
        highlight: false, // 行点击后highlight
    };

})(jQuery);