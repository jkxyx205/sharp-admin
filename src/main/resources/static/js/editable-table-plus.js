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
            if (this.options.rowFocus) {
                this.$table.delegate('tr', 'focus', (e) => {
                    let $tr = $(e.target).parents('tr')
                    if (this.focusRow !== $tr[0]) {
                        // 多次点击 保证只触发一次请求
                        this.options.rowFocus($tr, this._getValue($tr))
                        this.focusRow = $tr[0]
                    }
                })
            }
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
                    for (let i = 0; i < _this.options.columnConfigs.length; i++) {
                        let columnConfig = _this.options.columnConfigs[i];
                        let childIndex = i + 1 + (_this.options.showRowNumber ? 1 : 0)
                        let $input = $(this).find('td:nth-child('+childIndex+') input')

                        if (columnConfig.disabled === true) {
                            $input.prop('disabled', true).attr('readonly', true)
                        }
                    }
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
                    $(this).val(rowValue[this.name])
                })
            }
        },
        _getValue: function ($tr) {
            let value = {}
            $tr.find(':input').each(function () {
                let name = $(this).attr('name')
                if (name) {
                    value[name] = $(this).val()
                }
            })

            return value
        },
        _formatTr: function($tr) {
            for (let i = 0; i < this.options.columnConfigs.length; i++) {
                let columnConfig = this.options.columnConfigs[i];
                let childIndex = i + 1 + (this.options.showRowNumber ? 1 : 0)
                let $td = $tr.find('td:nth-child('+childIndex+')')
                let $input = $tr.find('td:nth-child('+childIndex+') input')
                $input.attr('name', columnConfig.name)
                    .css('text-align', columnConfig.align ?  columnConfig.align : 'left')

                if (columnConfig.type === 'text') {
                    if (columnConfig.disabled) {
                        $input.attr('disabled', true).attr('readonly', true)
                    }
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
                } else if (columnConfig.type === 'select') {
                    let options = []
                    for(let v of columnConfig.datasource) {
                        options.push('<option value="'+v.name+'">'+v.value+'</option>')
                    }
                    let $select = $('<select class="form-control"'+(columnConfig.disabled ? ' disabled' : '')+' name="'+columnConfig.name+'">'+options.join()+'</select>')

                    $tr.find('td:nth-child(' + childIndex + ')').html($select);
                    if (columnConfig.onchange) {
                        $select.on('change', function () {
                            columnConfig.onchange($tr, this.value)
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
                            return;
                        }
                    }).on('keyup', function (event) {
                        columnConfig.keyup && columnConfig.keyup($tr, event, $(this).val())
                    })
                } else if(columnConfig.type === 'hidden') {
                    $tr.append('<input type="hidden" name="'+columnConfig.name+'">')
                } else {
                    this.options.customizeType && this.options.customizeType[columnConfig.type] && this.options.customizeType[columnConfig.type].formatTr($td, columnConfig)
                }
            }

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

            for (let i = 0; i < this.options.columnConfigs.length; i++) {
                let columnConfig = this.options.columnConfigs[i];
                let childIndex = i + 1 + (this.options.showRowNumber ? 1 : 0)

                if (columnConfig.required === true) {
                    let $input = $requiredTr.find('td:nth-child('+childIndex+') :input')
                    let title = $input.attr('title') ? $input.attr('title') : '请填写' + columnConfig.title
                    $input.attr('required', true)
                        .attr('title', title)
                }
            }
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
        allowEmpty: false // 表格允许为空
    };

})(jQuery);