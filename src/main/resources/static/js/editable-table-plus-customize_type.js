// https://segmentfault.com/a/1190000015975240
;(function () {
    let customizeType = {
        'material': {
            mounted: function (columnConfig) {
                this.columnConfig = columnConfig

                if (!columnConfig.mode) {
                    columnConfig.mode = 'single'
                }
                
                let _this = this

                this.$dialogInput = $('#dialogInput').dialogInput({
                    title: '选择物料',
                    reportId: _this.columnConfig.reportId || '697147523487240192',
                    params: _this.columnConfig.params || undefined,
                    labelDisplay: function (row) {
                        return row.name + ' ' + this.specification(row)
                    },
                    specification: function (row) {
                        return !row.specification ? '' : row.specification;
                    },
                    selected: function (row) {
                        // console.log(row)
                        if ("createEvent" in document) {
                            var evt = document.createEvent("HTMLEvents");
                            evt.initEvent("input", false, true);
                            currentMaterialDom.dispatchEvent(evt);
                        }
                        else
                            currentMaterialDom.fireEvent("input");

                        currentMaterialDom.value = row.code
                        $(currentMaterialDom).siblings().val(row.id)


                        $(currentMaterialDom).parent().parent().find(':input[name=materialText]').val(this.labelDisplay(row))
                        $(currentMaterialDom).parent().parent().find(':input[name=materialName]').val(row.name)
                        $(currentMaterialDom).parent().parent().find(':input[name=materialSpecification]').val(this.specification(row))

                        $(currentMaterialDom).parent().parent().find(':input[name=materialCategoryId]').val(row.category_id)
                        $(currentMaterialDom).parent().parent().find(':input[name=unit]').val(row.base_unit)
                        $(currentMaterialDom).parent().parent().find(':input[name=unitText]').val(row.base_unit_name)

                        setCaretPosition(currentMaterialDom, currentMaterialDom.value.length)

                        _this.columnConfig.selected && _this.columnConfig.selected($(currentMaterialDom).parent().parent(), row)
                    }
                })

                this.$multipleDialogInput = $('#multipleDialogInput').dialogInput({
                    title: '选择物料',
                    reportId: _this.columnConfig.reportId || '697147523487240192',
                    params: _this.columnConfig.params || undefined,
                    mode: 'multiple', // 多选
                    labelDisplay: function (row) {
                        return row.name + ' ' + this.specification(row)
                    },
                    specification: function (row) {
                        return !row.specification ? '' : row.specification;
                    },
                    selected: function (rows) {
                        $editableTable.editableTablePlus('appendValue', rows.map(row => {
                            return {
                                "materialId": row.id,
                                "materialCode": row.code,
                                "materialText": this.labelDisplay(row),
                                "materialName": row.name,
                                "materialSpecification": this.specification(row),
                                "unit": row.base_unit,
                                "unitText": row.base_unit_name,
                                "materialCategoryId": row.category_id,
                            }
                        }))

                        _this.columnConfig.selected && _this.columnConfig.selected($(currentMaterialDom).parent().parent(), rows)
                    }
                })

                function setCaretPosition(ctrl, pos) {
                    // Modern browsers
                    if (ctrl.setSelectionRange) {
                        ctrl.focus();
                        ctrl.setSelectionRange(pos, pos);
                        // IE8 and below
                    } else if (ctrl.createTextRange) {
                        var range = ctrl.createTextRange();
                        range.collapse(true);
                        range.moveEnd('character', pos);
                        range.moveStart('character', pos);
                        range.select();
                    }
                }
            },
            formatTr: function ($td, columnConfig) {
                $td.find('input')
                    .attr('name', 'materialCode')
                    .attr('readonly', false)

                $td.append('<input class="form-control" type="hidden" name="'+columnConfig.name+'">')

                // 注册事件
                let _this = this
                $td.find('input[type=text]').on('click keydown', function(event){
                    // if ((event.keyCode && event.keyCode === 13) || !event.keyCode) {
                    if ((event.keyCode && event.keyCode !== 9) || !event.keyCode) {
                        currentMaterialDom = this
                        let context = {
                            params: {}
                        }
                        if (!_this.columnConfig.beforeShow || _this.columnConfig.beforeShow($(currentMaterialDom).parent().parent(), context)) {
                            // 获取客户端参数查询
                            if (columnConfig.mode === 'single' || $(this).parents("tr").next().length) {
                                // 设置参数
                                if (!$.isEmptyObject(context.params)) {
                                    _this.$dialogInput.dialogInput('setParams', context.params)
                                }
                                _this.$dialogInput.dialogInput().click()
                            } else {
                                // 设置参数
                                if (!$.isEmptyObject(context.params)) {
                                    _this.$multipleDialogInput.dialogInput('setParams', context.params)
                                }
                                _this.$multipleDialogInput.dialogInput().click()
                            }
                        }
                    }

                    if (event.keyCode !== 9) {
                        event.preventDefault();
                        return;
                    }
                })
            }
        }
    }


    window.customizeType = customizeType
})()



