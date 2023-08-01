(function($) {
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
                let width = columnConfig.width ? columnConfig.width + 'px' : 'auto'
                let align = columnConfig.align ? columnConfig.align  : 'left'

                columnTitles.push('<th style="width: ' + width + '; text-align: '+align+'">');
                columnTitles.push('<label'+(columnConfig.required ? " class=\"required\"" : "")+'>'+columnConfig.title+'</label>')
                columnTitles.push('</th>');
            }

            if (!this.options.readonly) {
                columnTitles.push('<th style="width: 50px;"></th>')
            }

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
                columns: this.options.columnConfigs.length + (this.options.showRowNumber ? 1 : 0),
                readonly: this.options.readonly,
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
            if (this.options.value != undefined && this.options.value.length > 0) {
                for (let rowValue of this.options.value) {
                    _this.$table.editableTable('addEmptyLine')
                    let $tr = this.options.readonly ? this.$table.find("tr:last-child") : this.$table.find("tr:last-child").prev()
                    $tr.find(":input[name]").each(function () {
                        $(this).val(rowValue[this.name])
                    })
                }

                if (this.options.readonly) {
                    this.$element.find('table.table-tbody :input').attr('disabled', true).attr('readonly', true)
                }
            }

            // 固定表头
            this.$element.table({fixedHead: true})
            if (!this.options.readonly) {
                $('.tr-empty.non-data').remove()
            }
        },
        getValue: function() {
            let valueList = []
            this.$table.find('tbody tr:not(:last-child)').each(function () {
                let value = {}
                $(this).find(':input').each(function () {
                    let name = $(this).attr('name')
                    if (name) {
                        value[name] = $(this).val()
                    }
                })
                valueList.push(value)
            })

            return valueList
        },
        _formatTr: function($tr) {
            for (let i = 0; i < this.options.columnConfigs.length; i++) {
                let columnConfig = this.options.columnConfigs[i];
                let childIndex = i + 1 + (this.options.showRowNumber ? 1 : 0)
                let $td = $tr.find('td:nth-child('+childIndex+')')

                if (columnConfig.type === 'number') {
                    $tr.find('td:nth-child('+childIndex+') input')
                        // .attr('type', 'number')
                        .attr('pattern', '^\\d+(\\.\\d{1,3})?$')
                        .attr('title', '必须数字')
                        .attr('name', columnConfig.name);
                } else if (columnConfig.type === 'text-label') {
                    $tr.find('td:nth-child('+childIndex+') input').attr('disabled', true).attr('readonly', true).attr("name", columnConfig.name + "Text")
                    $td.append('<input class="form-control" type="hidden" name="'+columnConfig.name+'">')
                } else if (columnConfig.type === 'text') {
                    let $input = $tr.find('td:nth-child(' + childIndex + ') input').attr('name', columnConfig.name)
                    if (columnConfig.disabled) {
                        $input.attr('disabled', true).attr('readonly', true)
                    }
                } else if (columnConfig.type === 'select') {
                    let options = []
                    for(let v of columnConfig.datasource) {
                        options.push('<option value="'+v.name+'">'+v.value+'</option>')
                    }
                    let $select = $('<select class="form-control"'+(columnConfig.disabled ? ' disabled' : '')+' name="'+columnConfig.name+'">'+options.join()+'</select>')

                    $tr.find('td:nth-child(' + childIndex + ')').html($select);
                    // onchange="'+columnConfig.onchange()+'"
                    if (columnConfig.onchange) {
                        $select.on('change', function () {
                            columnConfig.onchange($tr, this.value)
                        })
                    }
                } else {
                    this.options.customizeType && this.options.customizeType[columnConfig.type] && this.options.customizeType[columnConfig.type].formatTr($td, columnConfig)
                }
            }

            // id
            $tr.append('<input class="form-control" type="hidden" name="id">')
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
                    $requiredTr.find('td:nth-child('+childIndex+') :input').attr('required', true)
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