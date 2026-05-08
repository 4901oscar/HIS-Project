import Swal from 'sweetalert2';

const confirmBtn = 'px-5 py-2 rounded-lg font-semibold text-white bg-red-600 hover:bg-red-700 transition-colors';
const cancelBtn  = 'px-5 py-2 rounded-lg font-semibold text-gray-700 border border-gray-300 bg-white hover:bg-gray-50 transition-colors';
const okBtn      = 'px-5 py-2 rounded-lg font-semibold text-white bg-medin-cyan hover:bg-medin-blue transition-colors';

export const swalConfirm = Swal.mixin({
  buttonsStyling: false,
  customClass: {
    confirmButton: confirmBtn,
    cancelButton:  cancelBtn,
    actions: 'flex gap-3 mt-2',
  },
});

export const swalAlert = Swal.mixin({
  buttonsStyling: false,
  customClass: {
    confirmButton: okBtn,
    actions: 'mt-2',
  },
});
