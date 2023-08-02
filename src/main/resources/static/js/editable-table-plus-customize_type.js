// https://segmentfault.com/a/1190000015975240
;(function () {
    let customizeType = {
        'material': {
            formatTr: function ($td, columnConfig) {
                if (!columnConfig.mode) {
                    columnConfig.mode = 'single'
                }

                $td.find('input')
                    .attr('name', 'materialCode')
                    .attr('readonly', false)

                $td.append('<input class="form-control" type="hidden" name="'+columnConfig.name+'">')

                // 注册事件
                $td.find('input[type=text]').on('click keydown focus', function(event){
                    // if ((event.keyCode && event.keyCode === 13) || !event.keyCode) {
                    if ((event.keyCode && event.keyCode !== 9) || !event.keyCode) {
                        if (columnConfig.mode === 'single' || $(this).parents("tr").next().length) {
                            $dialogInput.click();
                            currentMaterialDom = this
                        } else {
                            $multipleDialogInput.click();
                        }
                    }

                    if (event.keyCode !== 9 ) {
                        event.preventDefault();
                        return;
                    }
                })
            }
        }
    }

    let $dialogInput = $('#dialogInput').dialogInput({
        title: '选择物料',
        reportId: '697147523487240192',
        labelDisplay: function (row) {
            return row.name + ' ' + (!row.characteristic ? '' : row.characteristic)
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

            $(currentMaterialDom).parent().next().find('input[type=text]').val(this.labelDisplay(row))
            $(currentMaterialDom).parents('tr').find(':input[name=materialCategoryId]').val(row.category_id)
            $(currentMaterialDom).parents('tr').find(':input[name=unit]').val(row.base_unit)
            $(currentMaterialDom).parents('tr').find(':input[name=unitText]').val(row.base_unit_name)

            setCaretPosition(currentMaterialDom, currentMaterialDom.value.length)

            let materialType = $editableTable.editableTablePlus('getColumnConfigs').find((c => c.type === 'material'))
            materialType.selected && materialType.selected($(currentMaterialDom).parents('tr'), row)
        }
    })

    let $multipleDialogInput = $('#multipleDialogInput').dialogInput({
        title: '选择物料',
        reportId: '697147523487240192',
        mode: 'multiple', // 多选
        labelDisplay: function (row) {
            return row.name + ' ' + (!row.characteristic ? '' : row.characteristic)
        },
        selected: function (rows) {
            $editableTable.editableTablePlus('appendValue', rows.map(row => {
                return {
                    "materialId": row.id,
                    "materialCode": row.code,
                    "materialText": this.labelDisplay(row),
                    "unit": row.base_unit,
                    "unitText": row.base_unit_name,
                    "materialCategoryId": row.category_id,
                }
            }))

            let materialType = $editableTable.editableTablePlus('getColumnConfigs').find((c => c.type === 'material'))
            materialType.selected && materialType.selected(null, rows)
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

    window.customizeType = customizeType
})()



